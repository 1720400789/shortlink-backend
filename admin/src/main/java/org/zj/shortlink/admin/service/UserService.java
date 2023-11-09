package org.zj.shortlink.admin.service;

import org.springframework.web.bind.annotation.RequestBody;
import org.zj.shortlink.admin.dao.entity.UserDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zj.shortlink.admin.dto.req.UserLoginReqDTO;
import org.zj.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.zj.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.zj.shortlink.admin.dto.resp.UserLoginRespDTO;
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

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return True or False
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     * @param requestParam 注册信息
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 用户个人信息修改
     * @param requestParam 修改参数
     */
    void update(@RequestBody UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     * @param requestParam 用户登录请求参数
     * @return 用户登录返回参数 Token
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 检查用户是否登录
     * @param token 用户登录Token
     * @return 用户是否登录标识
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出登录
     */
    void logout(String username, String token);
}
