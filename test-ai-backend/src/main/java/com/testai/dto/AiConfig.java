package com.testai.dto;

import lombok.Data;

/**
 * AI配置DTO
 * 用于配置调用AI服务的相关参数
 */
@Data
public class AiConfig {
    /** AI平台（aliyun/openai/gemini） */
    private String platform;
    
    /** API地址 */
    private String apiUrl;
    
    /** 模型名称 */
    private String model;
    
    /** API密钥 */
    private String apiKey;
}
