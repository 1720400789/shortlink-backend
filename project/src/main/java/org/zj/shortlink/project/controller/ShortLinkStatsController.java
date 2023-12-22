package org.zj.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zj.shortlink.project.common.convention.result.Result;
import org.zj.shortlink.project.common.convention.result.Results;
import org.zj.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkStatsRespDTO;
import org.zj.shortlink.project.service.ShortLinkStatsService;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam){
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }
}