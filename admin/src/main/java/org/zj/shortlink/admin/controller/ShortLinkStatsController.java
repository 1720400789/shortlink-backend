package org.zj.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.remote.ShortLinkActualRemoteService;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkGroupStatsReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;

/**
 * 短链接监控控制层
 */
@Slf4j
@RestController(value = "shortLinkStatsControllerAdmin")
@RequiredArgsConstructor
public class ShortLinkStatsController {

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
//    private final ShortLinkRemoteService shortLinkRemoteService =  new ShortLinkRemoteService() {};
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.oneShortLinkStats(requestParam.getFullShortUrl(), requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam){
        log.debug("短链接：{}", requestParam.getFullShortUrl());
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(requestParam.getFullShortUrl(), requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsRespDTO> groupShortLinkStats(ShortLinkGroupStatsReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStats(requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordRespDTO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }
}
