package org.zj.shortlink.admin.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.zj.shortlink.admin.common.biz.user.UserFlowRiskControlFilter;
import org.zj.shortlink.admin.common.biz.user.UserTransmitFilter;

/**
 * 用户配置自动装配
 * Configuration 注解表示该类是一个 Spring 的配置类
 * Bean 注解应该是通过工厂方法创建实体类，同时返回值都是 FilterRegistrationBean，所以装配的其实都是 FilterRegistrationBean 类
 * 方法的参数是类的成员变量，如 globalUserFlowRiskControlFilter 的成员变量就有 StringRedisTemplate 和 UserFlowRiskControlConfiguration
 * 同时 Spring 会从容器中寻找 StringRedisTemplate 和 UserFlowRiskControlConfiguration 的 Bean 并将其注入进来【【依赖注入】】
 */
@Configuration
public class UserConfiguration {

    /**
     * 用户信息传递过滤器
     */
    @Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter(StringRedisTemplate stringRedisTemplate) {
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserTransmitFilter(stringRedisTemplate));
        registration.addUrlPatterns("/*");
        registration.setOrder(0);
        return registration;
    }

    /**
     * 用户操作流量风控过滤器
     * ConditionalOnProperty 注释是条件判断，只有当 name 属性对应的配置为 havingValue 属性时才加载 Bean
     */
    @Bean
    @ConditionalOnProperty(name = "short-link.flow-limit.enable", havingValue = "true")
    public FilterRegistrationBean<UserFlowRiskControlFilter> globalUserFlowRiskControlFilter(
            StringRedisTemplate stringRedisTemplate,
            UserFlowRiskControlConfiguration userFlowRiskControlConfiguration) {
        FilterRegistrationBean<UserFlowRiskControlFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserFlowRiskControlFilter(stringRedisTemplate, userFlowRiskControlConfiguration));
        registration.addUrlPatterns("/*");
        // 设置过滤器优先级，一般优先级之间都有步长，不会挨着设置
        registration.setOrder(10);
        return registration;
    }
}
