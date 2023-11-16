package org.zj.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static org.zj.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短链接有效时间
     * @param validDate 有效期
     * @return 有效期时间戳
     */
    public static long getLinkCacheValidTime(Date validDate) {
        return Optional.ofNullable(validDate)
                // 如果有效期是存在的，就计算当前时间与到期时间间隔的毫秒期
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                // 如果没有有效期，在我们的系统中就默认是永久有效的，我们设置一个常量时间（一个月）
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }
}
