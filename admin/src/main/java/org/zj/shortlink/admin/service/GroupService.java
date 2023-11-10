package org.zj.shortlink.admin.service;

import org.zj.shortlink.admin.dao.entity.GroupDO;
import com.baomidou.mybatisplus.extension.service.IService;
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

    void saveGroup(String groupName);

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
}
