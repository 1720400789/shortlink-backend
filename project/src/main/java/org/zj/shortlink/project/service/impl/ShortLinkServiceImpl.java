package org.zj.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Week;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.zj.shortlink.project.common.convention.exception.ServiceException;
import org.zj.shortlink.project.common.enums.ValiDateTypeEnum;
import org.zj.shortlink.project.dao.entity.*;
import org.zj.shortlink.project.dao.mapper.*;
import org.zj.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.zj.shortlink.project.service.ShortLinkService;
import org.springframework.stereotype.Service;
import org.zj.shortlink.project.toolkit.HashUtil;
import org.zj.shortlink.project.toolkit.LinkUtil;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.zj.shortlink.project.common.constant.RedisKeyConstant.*;
import static org.zj.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

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

    private final LinkAccessStatsMapper linkAccessStatsMapper;

    private final LinkLocaleStatsMapper linkLocaleStatsMapper;

    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkBrowserStatsMapper;

    private final LinkAccessLogsMapper linkAccessLogsMapper;

    private final LinkDeviceStatsMapper linkDeviceStatsMapper;

    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocalAmapKey;

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
        String favicon = getFavicon(requestParam.getOriginUri());
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
                .fullShortUrl(fullShortUrl)
                .favicon(StrUtil.isBlank(favicon) ? "未知" : favicon)
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
                            .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
            ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
            if (hasShortLinkDO != null) {
                log.warn("短链接：{} 重复入库", fullShortUrl);
                throw new ServiceException("短链接生成重复");
            }
        }

        // 缓存预热
        // 将成功入库的短链接同步进布隆过滤器，并加载进缓存
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUri(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()),
                TimeUnit.MILLISECONDS);
        boolean add = shortUriCreateRegisterCachePenetrationBloomFilter.add(fullShortUrl);
        if (!add) {
            throw new ClassCastException("同步布隆过滤器失败！");
        }
        log.debug("create shortLink: fullShortUrl - {}", fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                // TODO 域名管理，检查当前用户是否可以用这个域名，协议可以从域名记录中拿，这里测试就暂时写死为http
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
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
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
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
                .fullShortUrl(hasShortLinkDO.getFullShortUrl())
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
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, requestParam.getOldGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    // 如果过期类型为永久不过期，就把 ValidDate 置为 Null
                    .set(Objects.equals(requestParam.getValidDateType(), ValiDateTypeEnum.PERMANENT.getType()), ShortLinkDO::getValidDate, null);
            baseMapper.update(shortLinkDO, updateWrapper);
        } else {
            // 如果要改变分组
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
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
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String fullShortUrl = serverName + "/" + shortUri;
        log.warn("短链接" + fullShortUrl + "重定向");

        // 检查缓存是否存在
        String fullShortKey = String.format(GOTO_SHORT_LINK_KEY, fullShortUrl);
        String oringinalLink = stringRedisTemplate.opsForValue().get(fullShortKey);
        // 缓存存在
        if (StrUtil.isNotBlank(oringinalLink)) {
            // 如果有缓存就直接跳转到原始链接
            shortLinkStats(fullShortUrl, null, request, response);
            ((HttpServletResponse) response).sendRedirect(oringinalLink);
            return ;
        }

        // 如果缓存中不存在
        // 查询布隆过滤器中是否存在，注意这里是直接查询 fullShortUrl 是否在布隆过滤器中，对应上面 createShortLink 中也是同步 fullShortLink 到布隆过滤器中
        boolean contains = shortUriCreateRegisterCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            // 布隆过滤器判断不存在是不会存在误判的，所以这里不会让正常链接跳转不成功
            // 重定向到我们设置的提示页面
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return ;
        }

        // 如果布隆过滤器中很可能存在（但是注意这里可能误判导致恶意请求通过）
        // 这里我们还会提前缓存空值，为的就是防止恶意攻击用缓存和数据库中都不存在的空值来缓存穿透攻击我们数据库
        String gotoIsNullKey = String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl);
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(gotoIsNullKey);
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            // 如果空值key非空，就说明这个请求是恶意的，只是之前被拦截过一次了，所以得打回恶意请求
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
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
                shortLinkStats(fullShortUrl, null, request, response);
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
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return ;
            }

            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);

            if (shortLinkDO == null || shortLinkDO.getValidDate().before(new Date())) {
                // 如果短链接不是永久的，并且有效期已经在当前时间之前了，就直接和上面应对 “缓存和数据库中都不存在的情况” 一样的处理
                stringRedisTemplate.opsForValue().set(gotoIsNullKey, "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return ;
            }
            // 如果查询到了就存入缓存中，这里也应该设置相对应的缓存时间
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUri(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS);
            shortLinkStats(fullShortUrl, shortLinkDO.getGid(), request, response);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUri());
        } finally {
            lock.unlock();
        }
    }

    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request , ServletResponse response) {
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        String uvCookieKey = "short-link:stats:uv:" + fullShortUrl;
        try {
            AtomicReference<String> uv = new AtomicReference<>();
            // 将设置 cookie 头封装成任务
            Runnable addResponseCookieTask = () -> {
                // 将唯一标识作为 cookie 头返回
                uv.set(UUID.fastUUID().toString());
                Cookie uvCookie = new Cookie("uv", uv.get());
                uvCookie.setMaxAge(60 * 60 * 24 * 30); // 单位是秒，这里表示浏览器的cookie保存一个月，因为考虑到短链接一般一个月后同一个用户都不会访问了
                uvCookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
                ((HttpServletResponse) response).addCookie(uvCookie);
                uvFirstFlag.set(Boolean.TRUE);
                // TODO 等待重构
                // 这里的设计是有问题的，短链接可能成千上万，那更不要说 uvCookieKey，如果每个新浏览器访问一个新 fullShortUrl 都要维护一个这样的集合，那 redis 会很快被打崩的
                stringRedisTemplate.opsForSet().add(uvCookieKey, uv.get());
            };
            // 如果 cookie 非空
            if (ArrayUtil.isNotEmpty(cookies)) {
                // 则遍历检查 uv 头
                Arrays.stream(cookies)
                        .filter(each -> Objects.equals(each.getName(), "uv"))
                        .findFirst()
                        .map(Cookie::getValue)
                        // 如果 uv cookie 头存在，则
                        .ifPresentOrElse(each -> {
                            uv.set(each);
                            Long uvAdded = stringRedisTemplate.opsForSet().add(uvCookieKey, each);
                            uvFirstFlag.set(uvAdded != null && uvAdded > 0L);
                        }, addResponseCookieTask); // uv cookie 头不存在则说明该浏览器第一次访问该 fullShortUrl ，执行任务
            } else {
                // 如果 cookie 都不存在，因为用户是直接通过 8001 端口重定向的，可能请求中根本就没有任何 cookie ，则执行任务
                addResponseCookieTask.run();
            }
            String remoteAddr = LinkUtil.getActualIp(((HttpServletRequest) request));
            Long uipAdded = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGotoDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                        .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
                ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGotoDO.getGid();
            }
            int hour = DateUtil.hour(new Date(), true);
            Week week = DateUtil.dayOfWeekEnum(new Date());
            int weekValue = week.getIso8601Value();
            LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                    .pv(1)
                    /**
                     * 情况一，unique_idx 不存在，则直接插入
                     * 情况二，unique_idx 存在，则应该对 uv 作判断新增了，如果是一个全新浏览器访问则 uv +1, 否则 +0
                     */
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hour)
                    .weekday(weekValue)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStatsDO);
            // 将需要知道属地的 ip 以及 api-key 包装
            Map<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocalAmapKey);
            localeParamMap.put("ip", remoteAddr);
            // 请求高德的 api
            String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
            // 解析返回的json数据
            JSONObject localeResultObj = JSON.parseObject(localeResultStr);
            String infocode = localeResultObj.getString("infocode");
            String actualProvince;
            String actualCity;
            if (StrUtil.isNotBlank(infocode) && StrUtil.equals(infocode, "10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.equals(province, "[]");
                LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .gid(gid)
                        .date(new Date())
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
                String os = LinkUtil.getOs((HttpServletRequest) request);
                LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                        .os(os)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);
                String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
                LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                        .browser(browser)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);
                String device = LinkUtil.getDevice((HttpServletRequest) request);
                LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                        .device(device)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);
                String network = LinkUtil.getNetwork(((HttpServletRequest) request));
                LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                        .network(network)
                        .cnt(1)
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .date(new Date())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);
                LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                        .ip(remoteAddr)
                        .browser(browser)
                        .os(os)
                        .network(network)
                        .device(device)
                        .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                        .gid(gid)
                        .fullShortUrl(fullShortUrl)
                        .user(uv.get())
                        .build();
                linkAccessLogsMapper.insert(linkAccessLogsDO);
            }
        } catch (Throwable ex) {
            log.error("短链接访问量统计异常", ex);
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

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }
}




