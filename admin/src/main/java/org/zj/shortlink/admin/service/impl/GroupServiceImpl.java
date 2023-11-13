package org.zj.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.zj.shortlink.admin.common.biz.user.UserContext;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.dao.entity.GroupDO;
import org.zj.shortlink.admin.dto.req.ShortLinkGroupSortReqDTO;
import org.zj.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import org.zj.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import org.zj.shortlink.admin.remote.ShortLinkRemoteService;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.zj.shortlink.admin.service.GroupService;
import org.zj.shortlink.admin.dao.mapper.GroupMapper;
import org.springframework.stereotype.Service;
import org.zj.shortlink.admin.toolkit.RandomGenerator;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
* @author 1720400789
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2023-11-10 16:07:04
*/
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        String gid = "";
        do {
            gid = RandomGenerator.generateRandomString();
        } while (!hasNotUsedGid(username, gid));

        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .username(username)
                .name(groupName)
                .sortOrder(0)
                .build();

        baseMapper.insert(groupDO);
    }

    //
    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        log.warn("username: {}", UserContext.getUsername());

        // t_group 分片键是 username
        // 拼接 SQL from t_group where username = UserContext.getUsername() and del_flag = 0 order by updateTime desc;
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                // TODO 获取用户名
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0)
                .orderByDesc(GroupDO::getSortOrder, GroupDO::getUpdateTime);

        // 获得对应 username 的 Group 集合，但是还差了一个 短链接数量，也就是每个 group 中短链接的数量
        List<GroupDO> groupDOList = baseMapper.selectList(lambdaQueryWrapper);

        // 利用hutool的httpget调用 project 模块的 count 方法
        // 将ShortLinkGroupRespDTO集合以stream流的形式取出其中的gid并组成List集合
        Result<List<ShortLinkGroupCountQueryRespDTO>> listResult = shortLinkRemoteService.
                listGroupShortLinkCount(groupDOList.stream().map(GroupDO::getGid).toList());

        // 将 SQL 执行结果分装到响应结果集
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList, ShortLinkGroupRespDTO.class);

        // 遍历响应结果集，将对应 gid 的 count 封装进去
        shortLinkGroupRespDTOList.forEach(each -> {
            String gid = each.getGid();
            Optional<ShortLinkGroupCountQueryRespDTO> first = listResult.getData().stream().
                    filter(item -> Objects.equals(item.getGid(), gid))
                    .findFirst();
            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
        });

        // 返回封装后的响应结果集
        return shortLinkGroupRespDTOList;
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getGid, gid)
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO, updateWrapper);
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, updateWrapper);
        });
    }

    /**
     * 判断gid是否已经使用过了
     * @param gid 生成的随机gid
     * @return true就是没用使用过
     */
    private boolean hasNotUsedGid(String username, String gid) {
        // 检查gid是否已经用过了
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                // TODO 这里username先暂时设置为null，后面写了网关从网关传
                .eq(GroupDO::getUsername, Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO hasGroupFlag = baseMapper.selectOne(lambdaQueryWrapper);

        return hasGroupFlag == null;
    }
}
