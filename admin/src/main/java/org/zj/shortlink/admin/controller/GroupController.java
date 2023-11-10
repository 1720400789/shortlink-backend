package org.zj.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.common.convention.result.Results;
import org.zj.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.zj.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import org.zj.shortlink.admin.service.GroupService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/short-link")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 新增短链接分组
     */
    @PostMapping("/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }

    /**
     * 获取短链接分组
     * 会主动从用户上下文UserContext中获取username然后后去对应用户创建的短链接分组
     */
    @GetMapping("/v1/group")
    public Result<List<ShortLinkGroupRespDTO>> listGroup() {
        return Results.success(groupService.listGroup());
    }


}
