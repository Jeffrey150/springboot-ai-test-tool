package com.testai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务详情 DTO
 */
@Data
public class TaskDetailDTO {

    /**
     * 任务 ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 文档类型
     */
    private String documentType;

    /**
     * 任务状态 1-进行中 2-已完成 3-任务失败 4-已评价
     */
    private Integer taskStatus;

    /**
     * 用例生成数量
     */
    private Integer resultCount;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

    /**
     * 任务持续时间（秒）
     */
    private Integer duration;

    /**
     * 质量评分
     */
    private BigDecimal qualityScore;

    /**
     * AI 响应内容
     */
    private String aiResponse;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;
}
