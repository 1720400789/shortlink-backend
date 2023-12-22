package org.zj.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.zj.shortlink.project.dao.entity.LinkAccessStatsDO;
import org.zj.shortlink.project.service.LinkAccessStatsService;
import org.zj.shortlink.project.dao.mapper.LinkAccessStatsMapper;
import org.springframework.stereotype.Service;

/**
* @author 1720400789
* @description 针对表【t_link_access_stats】的数据库操作Service实现
* @createDate 2023-11-20 10:23:22
*/
@Service
public class LinkAccessStatsServiceImpl extends ServiceImpl<LinkAccessStatsMapper, LinkAccessStatsDO>
    implements LinkAccessStatsService {

}




