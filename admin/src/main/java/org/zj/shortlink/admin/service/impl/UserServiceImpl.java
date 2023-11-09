package org.zj.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.zj.shortlink.admin.common.convention.exception.ClientException;
import org.zj.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.zj.shortlink.admin.dao.entity.UserDO;
import org.zj.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.zj.shortlink.admin.dto.resp.UserRespDTO;
import org.zj.shortlink.admin.service.UserService;
import org.zj.shortlink.admin.dao.mapper.UserMapper;
import org.springframework.stereotype.Service;

import static org.zj.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.zj.shortlink.admin.common.enums.UserErrorCodeEnum.*;

/**
* @author 1720400789
* @description 针对表【t_user】的数据库操作Service实现
* @createDate 2023-11-08 19:59:39
 * 用户接口实现层
*/
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;

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

    @Override
    public Boolean hasUsername(String username) {
//        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
//                .eq(UserDO::getUsername, username);
//        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
//        // 用户名存在返回True， 不存在返回False
//        return userDO != null;

        return userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());

        try {
            if (lock.tryLock()) {
                int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                if (insert < 1) {
                    throw new ClientException(USER_SAVE_ERROR);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                return ;
            }
            throw new ClientException(USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }
    }
}




