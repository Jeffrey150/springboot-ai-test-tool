package com.testai.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务查询请求 DTO
 */
@Data
public class TaskQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称（模糊查询）
     */
    private String taskName;

    /**
     * 文档类型
     */
    private String documentType;

    /**
     * 任务状态
     */
    private Integer taskStatus;

    /**
     * 创建时间开始
     */
    private String startTime;

    /**
     * 创建时间结束
     */
    private String endTime;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}
