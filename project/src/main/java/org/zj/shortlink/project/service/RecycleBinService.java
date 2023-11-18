package org.zj.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zj.shortlink.project.dao.entity.ShortLinkDO;
import org.zj.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站中的短链接
     * @param requestParam 分页查询参数
     * @return 分页集合
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);
}
