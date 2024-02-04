package org.zj.shortlink.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 短链接聚合应用
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
    "org.zj.shortlink.admin",
    "org.zj.shortlink.project",
    "org.zj.shortlink.aggregation"
})
@MapperScan(value = {
    "org.zj.shortlink.project.dao.mapper",
    "org.zj.shortlink.admin.dao.mapper"
})
public class AggregationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }
}