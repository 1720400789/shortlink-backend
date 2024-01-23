package org.zj.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.zj.shortlink.admin.common.convention.exception.ClientException;
import org.zj.shortlink.admin.common.enums.UserErrorCodeEnum;
import org.zj.shortlink.admin.dao.entity.UserDO;
import org.zj.shortlink.admin.dto.req.UserLoginReqDTO;
import org.zj.shortlink.admin.dto.req.UserRegisterReqDTO;
import org.zj.shortlink.admin.dto.req.UserUpdateReqDTO;
import org.zj.shortlink.admin.dto.resp.UserLoginRespDTO;
import org.zj.shortlink.admin.dto.resp.UserRespDTO;
import org.zj.shortlink.admin.service.GroupService;
import org.zj.shortlink.admin.service.UserService;
import org.zj.shortlink.admin.dao.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.zj.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static org.zj.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
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
    private final StringRedisTemplate stringRedisTemplate;
    private final GroupService groupService;

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

    /**
     * 用户注册接口实现
     * @param requestParam 注册信息
     */
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        // 使用布隆过滤器判断用户名是否存在
        // 布隆过滤器在判断是否存在时可能将不存在判断为存在，但是这个误判概率是可以接受的，而且对用户也影响不大，完全可以让用户再换一个名字
        if (hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }

        // redis分布式锁
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());

        try {
            // 获取锁成功则执行插入操作
            if (lock.tryLock()) {
                try {
                    int insert = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
                    // 插入失败则抛出客户端保存用户失败异常
                    if (insert < 1) {
                        throw new ClientException(USER_SAVE_ERROR);
                    }
                } catch (DuplicateKeyException ex) {
                    // 数据库层面要做一个 唯一索引 来兜底，以防最极端的情况
                    throw new ClientException(USER_EXIST);
                }
                // 新注册的用户同步到布隆过滤器
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
                // 每个用户创建后都有一个默认分组
                groupService.saveGroup(requestParam.getUsername(),"默认分组");
                return ;
            }
            throw new ClientException(USER_NAME_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        // 验证当前用户名是否为登录用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        // 查询用户是否存在
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getPassword, requestParam.getPassword())
                .eq(UserDO::getDelFlag, 0);
        UserDO result = baseMapper.selectOne(queryWrapper);

        if (result == null) {
            log.warn("用户不存在");
            throw new ClientException("用户不存在");
        }

        // 判断用户是否重复登录
        Map<Object ,Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + requestParam.getUsername());
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    // 如果 Redis 中没有用户登录信息，或者出现了其他问题导致获取失败，则直接抛出异常
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            //
            return new UserLoginRespDTO(token);
        }

        // 利用UUID生成一个唯一的用户Token
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + requestParam.getUsername(), uuid, JSON.toJSONString(result));
        // 设定30分钟有效期
        stringRedisTemplate.expire(USER_LOGIN_KEY + requestParam.getUsername(), 30L, TimeUnit.DAYS);

        // 返回用户Token
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return ;
        }
        throw new ClientException("用户token不存在或用户未登录");
    }
}




