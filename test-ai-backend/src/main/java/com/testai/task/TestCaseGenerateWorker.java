package com.testai.task;

import com.testai.dto.AiConfig;
import com.testai.service.AiService;
import com.testai.service.DocumentService;
import com.testai.service.TaskService;
import com.testai.service.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 测试用例生成工作器
 * 负责异步执行测试用例生成任务，包括文档内容提取、模板获取、AI调用等核心流程
 */
@Component
public class TestCaseGenerateWorker {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private AiService aiService;

    @Autowired
    private TaskService taskService;

    /** 每批调用的最大Token数 */
    @Value("${ai.batch.max-tokens:2048}")
    private int maxTokensPerBatch;

    /** 是否启用分批次调用 */
    @Value("${ai.batch.enable:true}")
    private boolean enableBatching;

    /**
     * 异步执行测试用例生成任务
     * 
     * @param taskId 任务ID
     * @param taskNo 任务号
     * @param aiConfig AI配置信息（平台、API地址、模型、API Key）
     */
    @Async
    public void executeGenerateTask(Long taskId, String taskNo, AiConfig aiConfig) {
        LocalDateTime startTime = LocalDateTime.now();

        try {
            // 更新任务状态为"进行中"
            taskService.updateTaskStatus(taskId, 1);

            // 获取任务详情
            com.testai.entity.Task task = taskService.getById(taskId);

            // 提取文档内容（支持单文档和多文档模式）
            String documentContent = extractDocumentContent(task);

            // 获取模板内容
            String templateContent = templateService.getContentById(task.getTemplateId());

            // 调用AI服务生成测试用例
            String aiResponse;
            if (enableBatching) {
                // 使用分批次调用（适用于大文档）
                aiResponse = aiService.callAiServiceWithBatching(documentContent, templateContent, taskNo, aiConfig, maxTokensPerBatch);
            } else {
                // 使用普通调用（适用于小文档）
                aiResponse = aiService.callAiService(
                    aiService.buildPrompt(documentContent, templateContent, taskNo), aiConfig);
            }
            
            // 保存AI生成的原始响应到数据库
            taskService.saveAiResponse(taskId, aiResponse);

            // 统计生成的测试用例数量（通过匹配"步骤"节点）
            int resultCount = countStepNodes(aiResponse);
            
            // 质量评分（暂未启用，设为null）
            Double qualityScore = null;

            // 计算耗时（秒）
            int duration = (int) Duration.between(startTime, LocalDateTime.now()).getSeconds();
            
            // 更新任务结果
            taskService.updateTaskResult(taskId, resultCount, qualityScore, duration);

        } catch (Exception e) {
            // 更新任务状态为"失败"
            taskService.updateTaskStatus(taskId, 3);
            throw new RuntimeException("用例生成任务执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 提取文档内容
     * 根据任务类型选择单文档或多文档模式
     * 
     * @param task 任务实体
     * @return 合并后的文档内容
     */
    private String extractDocumentContent(com.testai.entity.Task task) {
        if (task.getDocumentIds() != null && !task.getDocumentIds().isEmpty()) {
            // 多文档模式：合并所有文档内容，每个文档添加分隔标识
            StringBuilder mergedContent = new StringBuilder();
            String[] docIdArray = task.getDocumentIds().split(",");
            
            for (int i = 0; i < docIdArray.length; i++) {
                Long docId = Long.parseLong(docIdArray[i].trim());
                String content = documentService.extractContent(docId);
                
                mergedContent.append("=== 文档 ").append(i + 1).append(" ===\n");
                mergedContent.append(content);
                mergedContent.append("\n\n");
            }
            
            return mergedContent.toString();
        } else {
            // 单文档模式：直接返回单个文档内容
            return documentService.extractContent(task.getDocumentId());
        }
    }

    /**
     * 统计Markdown内容中的步骤节点数量
     * 每个"步骤"节点对应一个测试用例
     * 
     * @param markdown AI生成的Markdown内容
     * @return 步骤节点数量
     */
    private int countStepNodes(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return 0;
        }
        int count = 0;
        // 匹配以"- 步骤"开头的行
        Pattern pattern = Pattern.compile("(?m)^\\s*-\\s*步骤\\s*$");
        Matcher matcher = pattern.matcher(markdown);
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
