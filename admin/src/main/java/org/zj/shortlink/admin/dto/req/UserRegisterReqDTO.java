package org.zj.shortlink.admin.dto.req;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.zj.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;

/**
 * 用户注册请求参数
 */
@Data
public class UserRegisterReqDTO {

    private String username;

    private String realName;

    private String password;

    private String phone;

    private String mail;
}
