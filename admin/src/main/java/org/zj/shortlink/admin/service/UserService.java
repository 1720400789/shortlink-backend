package org.zj.shortlink.admin.service;

import org.zj.shortlink.admin.dao.entity.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zj.shortlink.admin.dto.resp.UserRespDTO;

/**
* @author 1720400789
* @description 针对表【t_user】的数据库操作Service
* @createDate 2023-11-08 19:59:39
*/
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);
}
