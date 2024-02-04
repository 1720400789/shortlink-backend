package org.zj.shortlink.admin.remote;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.remote.dto.req.*;
import org.zj.shortlink.admin.remote.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
@FeignClient("short-link-project")
public interface ShortLinkActualRemoteService {


//    default Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
//        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
//        return JSON.parseObject(resultBodyStr, new TypeReference<>() {});
//    }
    /**
     * 创建短链接
     * @param requestParam 创建短链接请求入参
     * @return 短链接创建响应
     */
    @PostMapping("/api/short-link/v1/create")
    Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 短链接批量创建响应
     */
    @PostMapping("/api/short-link/v1/create/batch")
    Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam);
//    default Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
//        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create/batch", JSON.toJSONString(requestParam));
//        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
//        });
//    }

    /**
     * 修改短链接
     * @param requestParam 修改短链接的请求参数
     */
    @PostMapping("/api/short-link/v1/update")
    void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam);
//    default void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
//        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSON.toJSONString(requestParam));
//    }

    /**
     * 分页查询短链接
     * @param gid      分组标识
     * @param orderTag 排序类型
     * @param current  当前页
     * @param size     当前数据多少
     * @return 分页集合
     */
    @GetMapping("/api/short-link/v1/page")
    Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestParam("gid") String gid,
                                                     @RequestParam("orderTag") String orderTag,
                                                     @RequestParam("current") Long current,
                                                     @RequestParam("size") Long size);
//    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam) {
//        // 因为最后调用的是GET，不能跟上面一样直接传一整个对象RequestBody
//        // 用Map集合装参数，调用hutool的HttpUtil.get时就会帮我们把Map序列化为Json字符串了
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put("gid", requestParam.getGid());
//        requestMap.put("orderTag", requestParam.getOrderTag());
//        requestMap.put("current", requestParam.getCurrent());
//        requestMap.put("size", requestParam.getSize());
//        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
//
//        return JSON.parseObject(resultPageStr, new TypeReference<>() {});
//    }

    /**
     * 查询分组短链接总量
     * @param requestParam 查询分组短链接总量参数
     * @return 查询分组短链接总量响应
     */
    @GetMapping("/api/short-link/v1/count")
    Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam);
//    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put("requestParam", requestParam);
//        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", requestMap);
//
//        return JSON.parseObject(resultPageStr, new TypeReference<>() {});
//    }

    /**
     * 根据 URL 获取标题
     @@ -123,106 +102,100 @@ public interface ShortLinkRemoteService {
      * @param url 目标网站地址
     * @return 网站标题
     */
    @GetMapping("/api/short-link/v1/title")
    Result<String> getTitleByUrl(@RequestParam("url") String url);
//    default Result<String> getTitleByUrl(@RequestParam("url") String url) {
//        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title?url=" + url);
//        return JSON.parseObject(resultStr, new TypeReference<>() {
//        });
//    }

    /**
     * 保存回收站
     * @param requestParam 请求参数
     * @return 返回集合
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    void saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam);
//    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
//        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSON.toJSONString(requestParam));
//    }

    /**
     * 分页查询回收站的短链接
     * @param gidList 分组标识集合
     * @param current 当前页
     * @param size    当前数据多少
     * @return 分页集合
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@RequestParam("gidList") List<String> gidList,
                                                               @RequestParam("current") Long current,
                                                               @RequestParam("size") Long size);
//    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(@RequestBody ShortLinkRecycleBinPageReqDTO requestParam) {
//        // 因为最后调用的是GET，不能跟上面一样直接传一整个对象RequestBody
//        // 用Map集合装参数，调用hutool的HttpUtil.get时就会帮我们把Map序列化为Json字符串了
//        Map<String, Object> requestMap = new HashMap<>();
//        requestMap.put("gidList", requestParam.getGidList());
//        requestMap.put("current", requestParam.getCurrent());
//        requestMap.put("size", requestParam.getSize());
//        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", requestMap);
//
//        return JSON.parseObject(resultPageStr, new TypeReference<>() {});
//    }

    /**
     * 恢复短链接
     * @param requestParam 短链接恢复请求参数
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    void recoverRecycleBin(@RequestBody RecycleBinRecoverReqDTO requestParam);
//    default void recoverRecycleBinShortLink(RecycleBinRecoverReqDTO requestParam) {
//        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSON.toJSONString(requestParam));
//    }

    /**
     * 移除短链接
     * @param requestParam 移除短链接请求参数
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    void removeRecycleBin(@RequestBody RecycleBinRemoveReqDTO requestParam);
//    default void removeRecycleBinShortLink(RecycleBinRemoveReqDTO requestParam) {
//        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
//    }

    /**
     * 访问单个短链接指定时间内监控数据
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param startDate    开始时间
     * @param endDate      结束时间
     * @return 短链接监控信息
     */
    @GetMapping("/api/short-link/v1/stats")
    Result<ShortLinkStatsRespDTO> oneShortLinkStats(@RequestParam("fullShortUrl") String fullShortUrl,
                                                    @RequestParam("gid") String gid,
                                                    @RequestParam("startDate") String startDate,
                                                    @RequestParam("endDate") String endDate);
//    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(ShortLinkStatsReqDTO requestParam) {
//        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", BeanUtil.beanToMap(requestParam));
//        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
//        });
//    }

    /**
     * 访问单个短链接指定时间内监控访问记录数据
     * @param fullShortUrl 完整短链接
     * @param gid          分组标识
     * @param startDate    开始时间
     * @param endDate      结束时间
     * @return 短链接监控访问记录信息
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(@RequestParam("fullShortUrl") String fullShortUrl,
                                                                               @RequestParam("gid") String gid,
                                                                               @RequestParam("startDate") String startDate,
                                                                               @RequestParam("endDate") String endDate);
//    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
//        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
//        stringObjectMap.remove("orders");
//        stringObjectMap.remove("records");
//        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record", stringObjectMap);
//        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
//        });
//    }

    /**
     * 访问分组短链接指定时间内监控数据
     * @param gid       分组标识
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 分组短链接监控信息
     */
    @GetMapping("/api/short-link/v1/stats/group")
    Result<ShortLinkStatsRespDTO> groupShortLinkStats(@RequestParam("gid") String gid,
                                                      @RequestParam("startDate") String startDate,
                                                      @RequestParam("endDate") String endDate);
//    default Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
//        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/group", BeanUtil.beanToMap(requestParam));
//        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
//        });
//    }

    /**
     * 访问分组短链接指定时间内监控访问记录数据
     * @param gid       分组标识
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 分组短链接监控访问记录信息
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(@RequestParam("gid") String gid,
                                                                                    @RequestParam("startDate") String startDate,
                                                                                    @RequestParam("endDate") String endDate);
//    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
//        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
//        stringObjectMap.remove("orders");
//        stringObjectMap.remove("records");
//        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record/group", stringObjectMap);
//        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
//        });
//    }
}
