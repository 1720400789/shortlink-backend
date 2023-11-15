package org.zj.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.zj.shortlink.project.common.convention.exception.ServiceException;
import org.zj.shortlink.project.common.enums.ValiDateTypeEnum;
import org.zj.shortlink.project.dao.entity.ShortLinkDO;
import org.zj.shortlink.project.dao.entity.ShortLinkGotoDO;
import org.zj.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import org.zj.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.zj.shortlink.project.service.ShortLinkService;
import org.zj.shortlink.project.dao.mapper.ShortLinkMapper;
import org.springframework.stereotype.Service;
import org.zj.shortlink.project.toolkit.HashUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.zj.shortlink.project.common.constant.RedisKeyConstant.*;

/**
* @author 1720400789
* @description 针对表【t_link】的数据库操作Service实现
* @createDate 2023-11-11 14:12:12
 * 短链接接口实现层
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateRegisterCachePenetrationBloomFilter;

    private final ShortLinkGotoMapper shortLinkGotoMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedissonClient redissonClient;

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 获得短链接
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(requestParam.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .originUri(requestParam.getOriginUri())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .fullShortUri(fullShortUrl)
                .build();
        ShortLinkGotoDO shortLinkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            baseMapper.insert(shortLinkDO);
            shortLinkGotoMapper.insert(shortLinkGotoDO);
        } catch (DuplicateKeyException ex) {
            // 情况1：短链接确实存在缓存中
            // 情况2：短链接不存在缓存中

            /**
             * 看起来这一段代码好像有点脱裤子放屁，我们本来就是MySQL报唯一索引的异常了，就说明
             */
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                            .eq(ShortLinkDO::getFullShortUri, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasShortLinkDO != null) {
                log.warn("短链接：{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }

        // 将成功入库的短链接同步进布隆过滤器
        shortUriCreateRegisterCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                // TODO 域名管理，检查当前用户是否可以用这个域名，协议可以从域名记录中拿，这里测试就暂时写死为http
                .fullShortUrl("http://" + shortLinkDO.getFullShortUri())
                .originUri(requestParam.getOriginUri())
                .gid(requestParam.getGid())
                .build();
    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
        // 根据 gid、FullShortUri 查询记录是否存在
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUri, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getOldGid())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);

        // 如果要修改的记录不存在则抛出异常
        // 其实我认为这里应该提前再做一层校验，如 gid 位数 == 6，DelFlag == 0等，不然这样直接查MySQL会对数据库有比较大的压力
        if (hasShortLinkDO == null) {
            throw new ClassCastException("短链接记录不存在");
        }

        // 如果要修改的记录存在
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(hasShortLinkDO.getDomain())
                .fullShortUri(hasShortLinkDO.getFullShortUri())
                .shortUri(hasShortLinkDO.getShortUri())
                .clickNum(hasShortLinkDO.getClickNum())
                .favicon(hasShortLinkDO.getFavicon())
                .createdType(hasShortLinkDO.getCreatedType())
                .gid(requestParam.getNewGid())
                .originUri(requestParam.getOriginUri())
                .describe(requestParam.getDescribe())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .build();
        // 如果短链接不改变分组
        if (Objects.equals(hasShortLinkDO.getGid(), requestParam.getNewGid())) {
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUri, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getOldGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    // 如果过期类型为永久不过期，就把 ValidDate 置为 Null
                    .set(Objects.equals(requestParam.getValidDateType(), ValiDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            // 如果要改变分组
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUri, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            baseMapper.delete(updateWrapper);
            baseMapper.insert(shortLinkDO);
        }
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页查询参数
     * @return 分页查询响应
     */
    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getCreateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    /**
     * 根据gid集合返回对应group的短链接数量
     * @param requestParam gid集合
     * @return git对应短链接数量集合
     */
    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    /**
     * 短链接跳转
     * 需要解决的问题：
     * 缓存击穿：某个时刻缓存失效了，同时大量的请求查询这个缓存，大量的查询就会短时间内打到数据库中
     *      解决方案：分布式锁
     * 缓存穿透：恶意请求缓存和数据库中一定不存在的数据，如此以来每次请求就一定会打到数据库上，而缓存对此无能为力
     *      解决方案：
     *          1、空对象值缓存
     * @param shortUri 短链接后缀
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;

        // 检查缓存是否存在
        String fullShortKey = String.format(GOTO_SHORT_LINK_KEY, fullShortUrl);
        String oringinalLink = stringRedisTemplate.opsForValue().get(fullShortKey);
        // 缓存存在
        if (StrUtil.isNotBlank(oringinalLink)) {
            // 如果有缓存就直接跳转到原始链接
            ((HttpServletResponse) response).sendRedirect(oringinalLink);
            return ;
        }

        // 查询布隆过滤器中是否存在
        boolean contains = shortUriCreateRegisterCachePenetrationBloomFilter.contains(fullShortKey);
        if (!contains) {
            // 布隆过滤器判断不存在是不会存在误判的，所以这里不会让正常链接跳转不成功
            return ;
        }

        // 如果布隆过滤器中存在（但是注意这里可能误判导致恶意请求通过）
        // 这里我们还会提前缓存空值，为的就是防止恶意攻击用缓存和数据库中都不存在的空值来缓存穿透攻击我们数据库
        String gotoIsNullKey = String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortKey);
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(gotoIsNullKey);
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            // 如果空值key非空，就说明这个请求是恶意的，只是之前被拦截过一次了，所以得打回恶意请求
            return ;
        }

        // 缓存不存在
        // 如果缓存为空就可能要去查询数据库了
        // 但是这里要考虑到缓存击穿和缓存穿透的问题
        // 分布式锁应对缓存击穿问题
        String lockKey = String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl);
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            oringinalLink = stringRedisTemplate.opsForValue().get(fullShortKey);
            // 双重判定锁
            // 上面之所以拿锁就是因为缓存不存在，但是在多线程高并发的情况下，可能某些线程在判断完缓存不存在之后，缓存就由其他的线程填充好了，所以这里再判定一次，以减少一些数据库查询的消耗
            if (StrUtil.isNotBlank(oringinalLink)) {
                ((HttpServletResponse) response).sendRedirect(oringinalLink);
                return ;
            }

            // 如果缓存依然不存在就只能查询数据库了
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null) {
                // 请求在缓存和数据库中都不存在，为了防止缓存穿透，这里要把值保存进缓存中
                stringRedisTemplate.opsForValue().set(gotoIsNullKey, "-", 30, TimeUnit.MINUTES);
                // 严谨来讲这里需要进行风险控制
                return ;
            }
            // TODO 这里还要考虑短链接是否过期的问题，如果过期了就直接返回错误就可以了
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUri, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);

            if (shortLinkDO != null) {
                // 如果查询到了就存入缓存中
                // TODO 这里也应该设置相对应的时间
                stringRedisTemplate.opsForValue().set(fullShortKey, shortLinkDO.getOriginUri());
                ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUri());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据全域名获得对应6位长度的短链接
     * @param requestParam 短链接参数中有originUrl
     * @return 返回短链接
     */
    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        String shortUri = "";
        int customGenerateCount = 0;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUri();
            // 加上当前系统的时间毫秒数，使得即使是同一链接也能尽量生成不同的短链接
            originUrl += System.currentTimeMillis();
            shortUri = HashUtil.hashToBase62(originUrl);
            String fullShortUri = requestParam.getDomain() + "/" + shortUri;

            // 走布隆过滤器
            if (!shortUriCreateRegisterCachePenetrationBloomFilter.contains(fullShortUri)) {
                // 布隆过滤器中不存在就说明生成的短链接可以用
                break;
            }

            customGenerateCount ++;
        }
        return shortUri;
    }
}




