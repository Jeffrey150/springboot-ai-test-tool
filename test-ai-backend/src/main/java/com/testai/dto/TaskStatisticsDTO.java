package com.testai.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务统计信息 DTO
 */
@Data
public class TaskStatisticsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总任务数
     */
    private Long totalCount;

    /**
     * 成功任务数
     */
    private Long successCount;

    /**
     * 失败任务数
     */
    private Long failedCount;

    /**
     * 进行中任务数
     */
    private Long processingCount;
}
