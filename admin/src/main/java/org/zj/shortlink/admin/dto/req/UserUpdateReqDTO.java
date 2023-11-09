package org.zj.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户注册请求参数
 */
@Data
public class UserUpdateReqDTO {

    private String username;

    private String realName;

    private String password;

    private String phone;

    private String mail;
}
