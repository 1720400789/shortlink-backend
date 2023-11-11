package org.zj.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.zj.shortlink.project.dao.entity.ShortLinkDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zj.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
* @author 1720400789
* @description 针对表【t_link】的数据库操作Service
* @createDate 2023-11-11 14:12:12
*/
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询参数
     * @return 分页集合
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);
}
