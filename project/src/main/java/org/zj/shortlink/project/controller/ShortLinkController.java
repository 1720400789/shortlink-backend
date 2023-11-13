package org.zj.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.zj.shortlink.project.common.convention.result.Result;
import org.zj.shortlink.project.common.convention.result.Results;
import org.zj.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import org.zj.shortlink.project.service.ShortLinkService;

import java.util.List;

/**
 * 短链接控制层
 */
@Slf4j
@RestController
@RequestMapping("/api/short-link")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 新建短链接
     * @param requestParam 新建短链接请求参数
     * @return 短链接信息
     */
    @PostMapping("/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    @PostMapping("/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接参数
     * @return 分页集合
     */
    @GetMapping("/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        log.warn("分页请求参数：{}", requestParam.toString());
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 根据gid数组查询对应短链接分组的短链接数量
     * @param requestParam gid数组
     * @return 每个gid对应的分组的短链接数量的集合
     */
    @GetMapping("/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }
}
