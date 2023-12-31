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
 * 短链接今日统计实体
 * @TableName t_link_stats_today
 */
@TableName(value ="t_link_stats_today")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsTodayDO extends BaseDO implements Serializable {
    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    @TableField(value = "gid")
    private String gid;

    /**
     * 短链接
     */
    @TableField(value = "full_short_url")
    private String fullShortUrl;

    /**
     * 日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 今日PV
     */
    @TableField(value = "today_pv")
    private Integer todayPv;

    /**
     * 今日UV
     */
    @TableField(value = "today_uv")
    private Integer todayUv;

    /**
     * 今日IP数
     */
    @TableField(value = "today_uip")
    private Integer todayUip;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}