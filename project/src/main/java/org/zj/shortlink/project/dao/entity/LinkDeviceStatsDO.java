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
 * 统计访问设备实体
 * @TableName t_link_device_stats
 */
@TableName(value ="t_link_device_stats")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkDeviceStatsDO extends BaseDO implements Serializable {
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
     * 日期
     */
    @TableField(value = "date")
    private Date date;

    /**
     * 访问量
     */
    @TableField(value = "cnt")
    private Integer cnt;

    /**
     * 访问设备
     */
    @TableField(value = "device")
    private String device;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}