package org.zj.shortlink.admin.dto.req;

import lombok.Data;

@Data
public class ShortLinkGroupSortReqDTO {

    /**
     * 要排序的分组ID
     */
    private String gid;

    /**
     * 排序字段
     */
    private Integer sortOrder;
}
