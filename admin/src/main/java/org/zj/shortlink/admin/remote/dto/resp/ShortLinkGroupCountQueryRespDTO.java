package org.zj.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * 短链接分组查询返回
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 当前分组的短链接数量
     */
    private Integer shortLinkCount;
}
