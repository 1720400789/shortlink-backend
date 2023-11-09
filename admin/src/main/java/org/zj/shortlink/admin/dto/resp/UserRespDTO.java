package org.zj.shortlink.admin.dto.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import org.zj.shortlink.admin.common.serialize.PhoneDesensitizationSerializer;

import java.io.Serializable;
import java.util.Date;

/**
 * entity包下的DO类是直接映射数据库种的表的，不能直接返回给前端
 * 所以规约出RespDTO和ReqDTO来分别作为返回集和接收集
 * UserRespDTO 用户返回参数响应
 *
 * 脱敏信息
 */
@Data
public class UserRespDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    /**
     * 手机号
     * @JsonSerialize 序列化注解，配合我们自定义的手机号序列化器直接将返回信息的手机号做脱敏处理
     */
    @JsonSerialize(using = PhoneDesensitizationSerializer.class)
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
