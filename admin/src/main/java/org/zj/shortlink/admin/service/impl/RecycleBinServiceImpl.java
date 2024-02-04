package org.zj.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.zj.shortlink.admin.common.biz.user.UserContext;
import org.zj.shortlink.admin.common.convention.exception.ServiceException;
import org.zj.shortlink.admin.common.convention.result.Result;
import org.zj.shortlink.admin.dao.entity.GroupDO;
import org.zj.shortlink.admin.dao.mapper.GroupMapper;
import org.zj.shortlink.admin.remote.ShortLinkActualRemoteService;
import org.zj.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import org.zj.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import org.zj.shortlink.admin.service.RecycleBinService;

import java.util.List;

/**
 * URL 回收站接口实现层
 */
@Service(value = "recycleBinServiceImplByAdmin")
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;

    /**
     * 后续重构为 SpringCloud Feign 调用
     */
//    ShortLinkActualRemoteService shortLinkRemoteService = new ShortLinkActualRemoteService() {};
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam.getGidList(), requestParam.getCurrent(), requestParam.getSize());
    }
}
