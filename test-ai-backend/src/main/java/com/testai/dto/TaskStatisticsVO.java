package com.testai.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 任务统计响应VO
 */
@Data
public class TaskStatisticsVO implements Serializable {

    /** 已完成任务数量 */
    private Long completedTaskCount;

    /** 已评价任务数量 */
    private Long evaluatedTaskCount;

    /** 已完成任务的用例总量 */
    private Long completedTestCaseCount;

    /** 已评价任务的用例总量 */
    private Long evaluatedTestCaseCount;

    /** 已完成任务的总执行时长（秒） */
    private Long completedTotalDuration;

    /** 已评价任务的总执行时长（秒） */
    private Long evaluatedTotalDuration;
}
