package org.zj.shortlink.admin.remote.dto.req;

import lombok.Data;

import java.util.Date;

/**
 * 短链接创建请求对象
 * 因为短链接项目中，中台Project模块提供短链接服务，不直接给前端控制台提供调用接口，而是借由Admin模块远程调用实现
 */
@Data
public class ShortLinkCreateReqDTO {

    /**
     * 域名
     */
    private String domain;

    /**
     * 原始链接
     */
    private String originUri;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 创建类型 0：控制台创建 1：接口创建
     */
    private Integer createdType;

    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */
    private Integer validDateType;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 描述
     */
    private String describe;
}
