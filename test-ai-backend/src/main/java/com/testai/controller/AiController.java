package com.testai.controller;

import com.testai.common.Result;
import com.testai.dto.AiConfig;
import com.testai.dto.TaskCreateDTO;
import com.testai.entity.Task;
import com.testai.service.TaskService;
import com.testai.task.TestCaseGenerateWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * AI服务控制器
 * 提供AI测试用例生成相关接口
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TestCaseGenerateWorker testCaseGenerateWorker;

    /**
     * 生成测试用例（单文档模式）
     * 
     * @param request 生成请求参数
     * @return 任务ID
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody GenerateRequest request) {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setDocumentId(request.getDocumentId());
        taskCreateDTO.setTaskName(request.getTaskName() != null ? request.getTaskName() : "测试用例生成");
        taskCreateDTO.setTemplateId(request.getTemplateId());
        
        Task task = taskService.createTask(taskCreateDTO);
        
        // 异步执行测试用例生成任务
        testCaseGenerateWorker.executeGenerateTask(task.getId(), null, request.getAiConfig());
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId());
        
        return Result.success(result);
    }

    /**
     * 测试用例生成请求参数
     */
    @lombok.Data
    public static class GenerateRequest {
        /** 文档ID */
        private Long documentId;
        /** 任务名称 */
        private String taskName;
        /** 模板ID */
        private Long templateId;
        /** AI配置信息 */
        private AiConfig aiConfig;
    }
}
