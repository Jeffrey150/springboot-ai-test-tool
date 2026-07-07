package com.testai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 模板实体类
 * 对应数据库表 template
 */
@Data
@TableName("template")
public class Template {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 模板类型 */
    private String templateType;

    /** 模板内容 */
    private String content;

    /** 配置信息（JSON格式） */
    private String config;

    /** 状态（0-禁用，1-启用） */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
