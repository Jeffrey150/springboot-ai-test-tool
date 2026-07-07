package com.testai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务实体类
 * 对应数据库表 task
 */
@Data
@TableName("task")
public class Task implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联文档ID（单文档模式） */
    private Long documentId;

    /** 关联文档ID列表（多文档模式，逗号分隔） */
    private String documentIds;

    /** 文档类型 */
    private String documentType;

    /** 任务名称 */
    private String taskName;

    /** 任务号 */
    private String taskNo;

    /** 任务状态（0-待处理，1-进行中，2-已完成，3-失败） */
    private Integer taskStatus;

    /** 模板ID */
    private Long templateId;

    /** 配置信息（JSON格式） */
    private String config;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 耗时（秒） */
    private Integer duration;

    /** 生成的测试用例数量 */
    private Integer resultCount;

    /** 质量评分 */
    private Double qualityScore;

    /** 反馈内容 */
    private String feedback;

    /** AI生成的响应内容 */
    private String aiResponse;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
