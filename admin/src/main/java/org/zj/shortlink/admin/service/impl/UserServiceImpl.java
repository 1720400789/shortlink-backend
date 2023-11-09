package org.zj.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.zj.shortlink.admin.common.convention.exception.ClientException;
import org.zj.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.zj.shortlink.admin.dao.entity.UserDO;
import org.zj.shortlink.admin.dto.resp.UserRespDTO;
import org.zj.shortlink.admin.service.UserService;
import org.zj.shortlink.admin.dao.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author 1720400789
* @description 针对表【t_user】的数据库操作Service实现
* @createDate 2023-11-08 19:59:39
 * 用户接口实现层
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }
}




