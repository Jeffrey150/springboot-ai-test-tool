package com.testai.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充配置
 * 用于自动填充实体的创建时间和更新时间字段
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充字段
     * 
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新操作时自动填充字段
     * 
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}