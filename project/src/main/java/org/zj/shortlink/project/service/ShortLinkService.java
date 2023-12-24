package org.zj.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.zj.shortlink.project.dao.entity.ShortLinkDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zj.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkPageReqDTO;
import org.zj.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.zj.shortlink.project.dto.resp.ShortLinkPageRespDTO;

import java.util.List;

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
     * 修改短链接
      * @param requestParam 修改短链接请求参数
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询参数
     * @return 分页集合
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 根据gid数组查询对应短链接分组的短链接数量
     * @param requestParam gid数组
     * @return 每个gid对应的分组的短链接数量的集合
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 短链接跳转
     * @param shortUri 短链接后缀
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 批量创建短链接返回参数
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);
}
