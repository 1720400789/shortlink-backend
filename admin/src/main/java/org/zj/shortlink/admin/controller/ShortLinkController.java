package org.zj.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.common.convention.result.Results;
import org.zj.shortlink.admin.remote.ShortLinkActualRemoteService;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkBatchCreateReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkBaseInfoRespDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkBatchCreateRespDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.zj.shortlink.admin.toolkit.EasyExcelWebUtil;

import java.util.List;

@RestController
@RequestMapping("/api/short-link/admin")
@RequiredArgsConstructor
public class ShortLinkController {

//    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 新建短链接
     * @param requestParam 新建短链接请求参数
     * @return 短链接信息
     */
    @PostMapping("/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    @PostMapping("/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        shortLinkActualRemoteService.updateShortLink(requestParam);
        return Results.success();
    }

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接参数
     * @return 分页集合
     */
    @GetMapping("/v1/page")
    public Result<Page<ShortLinkPageRespDTO>> pageShortLink(@RequestBody ShortLinkPageReqDTO requestParam) {
        return shortLinkActualRemoteService.pageShortLink(requestParam.getGid(), requestParam.getOrderTag(), requestParam.getCurrent(), requestParam.getSize());
    }

    /**
     * 批量创建短链接
     */
    @SneakyThrows
    @PostMapping("/api/short-link/admin/v1/create/batch")
    public void batchCreateShortLink(@RequestBody ShortLinkBatchCreateReqDTO requestParam, HttpServletResponse response) {
        Result<ShortLinkBatchCreateRespDTO> shortLinkBatchCreateRespDTOResult = shortLinkActualRemoteService.batchCreateShortLink(requestParam);
        if (shortLinkBatchCreateRespDTOResult.isSuccess()) {
            List<ShortLinkBaseInfoRespDTO> baseLinkInfos = shortLinkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            // 将从 Sass 请求到的数据通过 EasyExcel 写入 resp 流中
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", ShortLinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }
}
