package org.zj.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import org.zj.shortlink.project.dao.entity.ShortLinkDO;

import java.util.List;

/**
 * 回收站短链接分页请求
 */
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识集合
     */
    private List<String> gidList;

}
