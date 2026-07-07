package com.testai.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务列表展示 VO
 * 用于任务管理页面的列表展示，支持多文档类型
 */
@Data
public class TaskListVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务号
     */
    private String taskNo;

    /**
     * 文档类型列表（多文档模式下可能包含多个）
     */
    private List<String> documentTypes;

    /**
     * 任务状态 1-进行中 2-已完成 3-任务失败 4-已评价
     */
    private Integer taskStatus;

    /**
     * 生成的测试用例数量
     */
    private Integer resultCount;

    /**
     * 质量评分
     */
    private Double qualityScore;

    /**
     * 任务持续时间（秒）
     */
    private Integer duration;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

    /**
     * 关联文档ID（单文档模式）
     */
    private Long documentId;

    /**
     * 关联文档ID列表（多文档模式，逗号分隔）
     */
    private String documentIds;
}
