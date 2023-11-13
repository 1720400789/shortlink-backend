package org.zj.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;
import org.zj.shortlink.project.common.convention.exception.ServiceException;
import org.zj.shortlink.project.common.enums.ValiDateTypeEnum;
import org.zj.shortlink.project.dao.entity.ShortLinkDO;
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

    private final ShortLinkMapper shortLinkMapper;

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    @Override
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
        try {
            baseMapper.insert(shortLinkDO);
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
        shortUriCreateRegisterCachePenetrationBloomFilter.add(shortLinkSuffix);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl(shortLinkDO.getFullShortUri())
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
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageRespDTO.class));
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




