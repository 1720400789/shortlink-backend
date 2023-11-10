package org.zj.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.common.convention.result.Results;
import org.zj.shortlink.admin.dto.req.ShortLinkGroupSaveReqDTO;
import org.zj.shortlink.admin.service.GroupService;

@Slf4j
@RestController
@RequestMapping("/api/short-link")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;


    @PostMapping("/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam) {
        groupService.saveGroup(requestParam.getName());
        return Results.success();
    }
}
