package org.zj.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.common.convention.result.Results;
import org.zj.shortlink.admin.remote.ShortLinkRemoteService;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

@RestController
@RequestMapping("/api/short-link/admin")
@RequiredArgsConstructor
public class ShortLinkController {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    /**
     * 新建短链接
     * @param requestParam 新建短链接请求参数
     * @return 短链接信息
     */
    @PostMapping("/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkRemoteService.createShortLink(requestParam);
    }

    @PostMapping("/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接参数
     * @return 分页集合
     */
    @GetMapping("/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam) {
        return shortLinkRemoteService.pageShortLink(requestParam);
    }
}
