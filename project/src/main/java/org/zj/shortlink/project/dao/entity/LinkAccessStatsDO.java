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
 * 短链接基础访问监控实体
 * @TableName t_link_access_stats
 */
@TableName(value ="t_link_access_stats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkAccessStatsDO extends BaseDO implements Serializable {
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
     * 访问的日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 访问量
     */
    @TableField(value = "pv")
    private Integer pv;

    /**
     * 独立访客数
     */
    @TableField(value = "uv")
    private Integer uv;

    /**
     * 独立ip数
     */
    @TableField(value = "uip")
    private Integer uip;

    /**
     * 小时
     */
    @TableField(value = "hour")
    private Integer hour;

    /**
     * 星期
     */
    @TableField(value = "weekday")
    private Integer weekday;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}