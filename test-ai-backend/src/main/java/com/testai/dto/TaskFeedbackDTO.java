package com.testai.dto;

import lombok.Data;

/**
 * 任务反馈请求DTO
 */
@Data
public class TaskFeedbackDTO {
    /** 评分 */
    private Integer rating;
    
    /** 反馈内容 */
    private String feedback;
    
    /** 采用率 */
    private Double adoptionRate;
}
