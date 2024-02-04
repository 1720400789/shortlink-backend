package org.zj.shortlink.project.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@ConditionalOnBean(MyMetaObjectHandler.class)
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("公共字段自动填充[insert]...");
        log.info(metaObject.toString());
        if (metaObject.hasSetter("createTime")){
            this.strictInsertFill(metaObject, "createTime", Date::new, Date.class);
        }
        if (metaObject.hasSetter("updateTime")) {
            this.strictInsertFill(metaObject, "updateTime", Date::new, Date.class);
        }
        if (metaObject.hasSetter("delFlag")) {
            this.strictInsertFill(metaObject, "delFlag", () -> 0, Integer.class);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long id = Thread.currentThread().getId();
        log.info("线程id为：{}", id);

        log.info("公共字段自动填充[update]...");
        log.info(metaObject.toString());
        if (metaObject.hasSetter("updateTime")) {
            this.strictUpdateFill(metaObject, "updateTime", Date::new, Date.class);
        }
    }
}
