package org.zj.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.zj.shortlink.admin.dao.entity.GroupDO;
import org.zj.shortlink.admin.service.GroupService;
import org.zj.shortlink.admin.dao.mapper.GroupMapper;
import org.springframework.stereotype.Service;

/**
* @author 1720400789
* @description 针对表【t_group】的数据库操作Service实现
* @createDate 2023-11-10 16:07:04
*/
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

}
