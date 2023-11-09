package org.zj.shortlink.admin.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录接口请求参数
 */
@Data
public class UserLoginReqDTO {

    private String username;

    private String password;

}
