package org.zj.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.zj.shortlink.admin.dao.entity.GroupDO;
import org.zj.shortlink.admin.service.GroupService;
import org.zj.shortlink.admin.dao.mapper.GroupMapper;
import org.springframework.stereotype.Service;
import org.zj.shortlink.admin.toolkit.RandomGenerator;

/**
* @author 1720400789
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2023-11-10 16:07:04
*/
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid = "";
        do {
            gid = RandomGenerator.generateRandomString();
        } while (!hasNotUsedGid(gid));

        GroupDO groupDO = GroupDO.builder()
                .gid(RandomGenerator.generateRandomString())
                .name(groupName)
                .build();

        baseMapper.insert(groupDO);
    }

    /**
     * 判断gid是否已经使用过了
     * @param gid 生成的随机gid
     * @return true就是没用使用过
     */
    private boolean hasNotUsedGid(String gid) {
        // 检查gid是否已经用过了
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                // TODO 这里username先暂时设置为null，后面写了网关从网关传
                .eq(GroupDO::getUsername, null);
        GroupDO hasGroupFlag = baseMapper.selectOne(lambdaQueryWrapper);

        return hasGroupFlag == null;
    }
}
