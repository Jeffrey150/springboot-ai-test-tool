package com.testai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务评价实体类
 */
@Data
@TableName("task_feedback")
public class TaskFeedback implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务 ID（外键，唯一）
     */
    private Long taskId;

    /**
     * 用例整体质量评分（1-5 分）
     */
    private Integer qualityScore;

    /**
     * 用例采纳率（0-100）
     */
    private Integer adoptionRate;

    /**
     * 用户建议
     */
    private String suggestion;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
