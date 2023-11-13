package org.zj.shortlink.admin.service;

import org.zj.shortlink.admin.dao.entity.GroupDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zj.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import org.zj.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.zj.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
* @author 1720400789
* @description 针对表【t_group】的数据库操作Service
* @createDate 2023-11-10 16:07:04
 * 短链接分组接口层
*/
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组，这里不必提供username，因为UserContext那边，用户登录后请求头会携带username
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);

    /**
     * 专门为用户注册提供的新增短链接分组接口
     * 因为用户注册成功的那一刻就默认提供一个默认分组，但是注册的时候是没有携带token等信息的，所以得重写一个
     * @param username 用户名
     * @param groupName 短链接分组名
     */
    void saveGroup(String username, String groupName);

    /**
     * 查询用户短连接分组集合
     * @return 短链接分组集合
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     * @param requestParam 短链接分组参数
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     * @param gid 短链接标识
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param requestParam 短链接分组排序参数
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
