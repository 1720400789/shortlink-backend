package org.zj.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.zj.shortlink.project.dao.entity.LinkAccessLogsDO;
import org.zj.shortlink.project.service.LinkAccessLogsService;
import org.zj.shortlink.project.dao.mapper.LinkAccessLogsMapper;
import org.springframework.stereotype.Service;

/**
* @author 1720400789
* @description 针对表【t_link_access_logs】的数据库操作Service实现
* @createDate 2023-12-19 19:20:00
*/
@Service
public class LinkAccessLogsServiceImpl extends ServiceImpl<LinkAccessLogsMapper, LinkAccessLogsDO>
    implements LinkAccessLogsService {

}




