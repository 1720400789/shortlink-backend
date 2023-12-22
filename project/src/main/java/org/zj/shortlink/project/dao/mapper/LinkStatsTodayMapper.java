package org.zj.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.zj.shortlink.project.dao.entity.LinkStatsTodayDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.zj.shortlink.project.dao.entity.ShortLinkDO;
import org.zj.shortlink.project.dto.req.ShortLinkPageReqDTO;

/**
 * 短链接今日统计持久层
* @author 1720400789
* @description 针对表【t_link_stats_today】的数据库操作Mapper
* @createDate 2023-12-22 15:40:56
* @Entity generator.domain.TLinkStatsToday
*/
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    /**
     * 记录地区访问监控数据
     */
    @Insert("INSERT INTO t_link_stats_today (full_short_url, gid, date, today_pv, today_uv,today_uip, create_time, update_time, del_flag) " +
            "VALUES( #{linkTodyStats.fullShortUrl}, #{linkTodyStats.gid}, #{linkTodyStats.date}, #{linkTodyStats.todayPv}, #{linkTodyStats.todayUv}, #{linkTodyStats.todayUip}, NOW(), NOW(), 0) " +
            "ON DUPLICATE KEY UPDATE today_pv = today_pv +  #{linkTodyStats.todayPv}, today_uv = today_uv +  #{linkTodyStats.todayUv}, today_uip = today_uip +  #{linkTodyStats.todayUip};")
    void shortLinkTodayState(@Param("linkTodyStats") LinkStatsTodayDO linkStatsTodayDO);

}




