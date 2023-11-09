package org.zj.shortlink.admin.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.common.convention.result.Results;
import org.zj.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.zj.shortlink.admin.dto.resp.UserRespDTO;
import org.zj.shortlink.admin.service.UserService;

/**
 * 用户管理控制层
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    /**
     * 配合lombok的@RequiredArgsConstructor，使用构造器注入，这里要优于@Autowired和@Resource注入
     * 一方面使用构造器注入可以使得原来的两行代码变为一行
     * 另一方面@Autowired会经常警告，不美观
     * 而@Resource在JDK17后改包了，所以也没有那么完美
     */
    private final UserService userService;
    /**
     * 通过 errorCode、errorMessage 构建失败响应
     */
    public static Result<Void> failure(String errorCode, String errorMessage) {
        return new Result<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        log.warn("username:{}", username);
        UserRespDTO result = userService.getUserByUsername(username);
        return Results.success(result);
    }
}
