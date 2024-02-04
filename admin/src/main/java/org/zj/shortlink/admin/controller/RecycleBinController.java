package org.zj.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.common.convention.result.Results;
import org.zj.shortlink.admin.remote.ShortLinkActualRemoteService;
import org.zj.shortlink.admin.remote.dto.req.RecycleBinRecoverReqDTO;
import org.zj.shortlink.admin.remote.dto.req.RecycleBinRemoveReqDTO;
import org.zj.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.zj.shortlink.admin.service.RecycleBinService;

/**
 * 回收站控制层
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/short-link/admin")
@RestController(value = "recycleBinControllerByAdmin")
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
//    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 保存回收站
     */
    @PostMapping("/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
//        shortLinkRemoteService.saveRecycleBin(requestParam);
        shortLinkActualRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站的短链接
     * @param requestParam 分页查询短链接参数
     * @return 分页集合
     */
    @GetMapping("/v1/recycle-bin/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkRecycleBinPageReqDTO requestParam) {
        return recycleBinService.pageRecycleBinShortLink(requestParam);
    }

    /**
     * 恢复回收站短连接
     * @param requestParam 要恢复的短链接的 gid 和 fullShortUrl
     * @return 标识
     */
    @PostMapping("/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBinShortLink(@RequestBody RecycleBinRecoverReqDTO requestParam) {
        shortLinkActualRemoteService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除回收站中的短链接
     * @param requestParam gid 和 fullShortUrl
     * @return 标识
     */
    @PostMapping("/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBinShortLink(@RequestBody RecycleBinRemoveReqDTO requestParam) {
        shortLinkActualRemoteService.removeRecycleBin(requestParam);
        return Results.success();
    }
}
