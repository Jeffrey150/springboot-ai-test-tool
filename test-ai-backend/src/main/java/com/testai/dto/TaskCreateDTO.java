package com.testai.dto;

import lombok.Data;

import java.util.List;

/**
 * 任务创建请求DTO
 */
@Data
public class TaskCreateDTO {
    /** 单文档ID（单文档模式） */
    private Long documentId;
    
    /** 多文档ID列表（多文档模式） */
    private List<Long> documentIds;
    
    /** 任务名称 */
    private String taskName;
    
    /** 任务号 */
    private String taskNo;
    
    /** 模板ID */
    private Long templateId;
    
    /** 配置信息（JSON格式） */
    private String config;
}
