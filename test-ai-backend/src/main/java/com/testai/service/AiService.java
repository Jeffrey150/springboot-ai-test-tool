package com.testai.service;

import com.testai.dto.AiConfig;
import java.util.List;

/**
 * AI服务接口
 * 提供测试用例生成、质量评估、AI调用等功能
 */
public interface AiService {

    /**
     * 生成测试用例
     * 
     * @param documentContent 文档内容
     * @param config AI配置
     * @param templateContent 模板内容
     * @return AI生成的测试用例内容
     */
    String generateTestCases(String documentContent, AiConfig config, String templateContent);

    /**
     * 评估测试用例质量
     * 
     * @param testCases 测试用例列表
     * @return 质量评分
     */
    Double evaluateQuality(List<String> testCases);

    /**
     * 解析AI响应
     * 
     * @param aiResponse AI响应内容
     * @return 解析后的内容
     */
    String parseAiResponse(String aiResponse);
    
    /**
     * 构建提示词（不带任务号）
     * 
     * @param documentContent 文档内容
     * @param templateContent 模板内容
     * @return 构建好的提示词
     */
    String buildPrompt(String documentContent, String templateContent);
    
    /**
     * 构建提示词（带任务号）
     * 
     * @param documentContent 文档内容
     * @param templateContent 模板内容
     * @param taskNo 任务号
     * @return 构建好的提示词
     */
    String buildPrompt(String documentContent, String templateContent, String taskNo);
    
    /**
     * 调用AI服务
     * 
     * @param prompt 提示词
     * @param config AI配置
     * @return AI响应内容
     */
    String callAiService(String prompt, AiConfig config);

    /**
     * 分批次调用AI服务
     * 
     * @param documentContent 文档内容
     * @param templateContent 模板内容
     * @param config AI配置
     * @param maxTokensPerBatch 每批次最大token数
     * @return 合并后的AI响应
     */
    String callAiServiceWithBatching(String documentContent, String templateContent, AiConfig config, int maxTokensPerBatch);
    
    /**
     * 分批次调用AI服务（带任务号）
     * 
     * @param documentContent 文档内容
     * @param templateContent 模板内容
     * @param taskNo 任务号
     * @param config AI配置
     * @param maxTokensPerBatch 每批次最大token数
     * @return 合并后的AI响应
     */
    String callAiServiceWithBatching(String documentContent, String templateContent, String taskNo, AiConfig config, int maxTokensPerBatch);
}
