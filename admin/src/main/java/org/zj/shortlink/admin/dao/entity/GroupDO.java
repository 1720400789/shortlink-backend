package org.zj.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.zj.shortlink.admin.common.database.BaseDO;

/**
 * 
 * @TableName t_group
 */
@Data
@TableName(value ="t_group")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDO extends BaseDO implements Serializable {
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
     * 分组名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 创建分组用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 分组排序
     */
    @TableField(value = "sort_order")
    private Integer sortOrder;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}