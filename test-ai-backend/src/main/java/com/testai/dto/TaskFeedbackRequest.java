package com.testai.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 任务评价请求 DTO
 */
@Data
public class TaskFeedbackRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    @NotNull(message = "任务 ID 不能为空")
    private Long taskId;

    /**
     * 用例整体质量评分（1-5 分）
     */
    @NotNull(message = "质量评分不能为空")
    @Min(value = 1, message = "质量评分最小为 1 分")
    @Max(value = 5, message = "质量评分最大为 5 分")
    private Integer qualityScore;

    /**
     * 用例采纳率（0-100）
     */
    @NotNull(message = "采纳率不能为空")
    @Min(value = 0, message = "采纳率最小为 0")
    @Max(value = 100, message = "采纳率最大为 100")
    private Integer adoptionRate;

    /**
     * 用户建议
     */
    private String suggestion;
}
