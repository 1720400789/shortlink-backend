package org.zj.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zj.shortlink.project.common.database.BaseDO;

/**
 * 
 * @TableName t_link_access_logs
 */
@TableName(value ="t_link_access_logs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkAccessLogsDO extends BaseDO implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 完整短链接
     */
    @TableField(value = "full_short_url")
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 用户信息
     */
    @TableField(value = "user")
    private String user;

    /**
     * 浏览器
     */
    @TableField(value = "browser")
    private String browser;

    /**
     * 操作系统
     */
    @TableField(value = "os")
    private String os;

    /**
     * IP
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 访问网络
     */
    @TableField(value = "network")
    private String network;

    /**
     * 访问设备
     */
    @TableField(value = "device")
    private String device;

    /**
     * 地区
     */
    @TableField(value = "locale")
    private String locale;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}