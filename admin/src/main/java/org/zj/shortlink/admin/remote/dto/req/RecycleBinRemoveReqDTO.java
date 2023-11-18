package org.zj.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 回收站中短链接恢复保存功能
 */
@Data
public class RecycleBinRemoveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接全部分
     */
    private String fullShortUrl;
}
