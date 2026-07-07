package com.testai.controller;

import com.testai.common.Result;
import com.testai.dto.AiConfig;
import com.testai.dto.TaskCreateDTO;
import com.testai.dto.TaskFeedbackDTO;
import com.testai.entity.Task;
import com.testai.service.TaskService;
import com.testai.task.TestCaseGenerateWorker;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testai.common.Result;
import com.testai.dto.TaskDetailDTO;
import com.testai.dto.TaskFileDTO;
import com.testai.dto.TaskListVO;
import com.testai.dto.TaskQueryRequest;
import com.testai.dto.TaskStatisticsDTO;
import com.testai.entity.Task;
import com.testai.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务管理控制器
 * 负责测试用例生成任务的创建、查询、导出等操作
 */
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TestCaseGenerateWorker testCaseGenerateWorker;

    /**
     * 生成测试用例
     * 支持单文档和多文档两种模式：
     * - 单文档模式：通过documentId指定单个文档
     * - 多文档模式：通过documentIds指定多个文档ID列表
     * 
     * @param request 生成请求参数，包含文档ID、任务信息、AI配置
     * @return 返回任务ID
     */
    @PostMapping("/generate")
    public Result<Map<String, Object>> generate(@RequestBody GenerateRequest request) {
        System.out.println("接收到生成请求:");
        System.out.println("  documentId: " + request.getDocumentId());
        System.out.println("  documentIds: " + request.getDocumentIds());
        System.out.println("  taskName: " + request.getTaskName());
        System.out.println("  taskNo: " + request.getTaskNo());
        
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTaskName(request.getTaskName() != null ? request.getTaskName() : "测试用例生成");
        taskCreateDTO.setTaskNo(request.getTaskNo());
        taskCreateDTO.setTemplateId(request.getTemplateId());

        Task task;
        if (request.getDocumentIds() != null && !request.getDocumentIds().isEmpty()) {
            // 多文档模式：合并多个文档内容进行处理
            System.out.println("使用多文档模式");
            taskCreateDTO.setDocumentIds(request.getDocumentIds());
            task = taskService.createMultiDocumentTask(taskCreateDTO);
        } else {
            // 单文档模式：使用单个文档内容
            System.out.println("使用单文档模式");
            taskCreateDTO.setDocumentId(request.getDocumentId());
            task = taskService.createTask(taskCreateDTO);
        }

        // 异步执行测试用例生成任务
        testCaseGenerateWorker.executeGenerateTask(task.getId(), request.getTaskNo(), request.getAiConfig());

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId());

        return Result.success(result);
    }

    /**
     * 创建任务
     * 
     * @param dto 任务创建参数
     * @return 创建的任务实体
     */
    @PostMapping("/create")
    public Result<Task> create(@RequestBody TaskCreateDTO dto) {
        return Result.success(taskService.createTask(dto));
    }

    /**
     * 根据ID查询任务详情
     * 
     * @param id 任务ID
     * @return 任务实体
     */
    @GetMapping("/{id}")
    public Result<Task> getById(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @PostMapping("/{id}/feedback")
    public Result<Void> submitFeedback(
            @PathVariable Long id,
            @RequestBody TaskFeedbackDTO dto) {
        taskService.submitFeedback(id, dto);
        return Result.success();
    }

    /**
     * 删除任务
     * 
     * @param id 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        taskService.deleteById(id);
        return Result.success();
    }

    /**
     * 导出AI生成的测试用例（Markdown格式）
     * 文件名优先级：任务号 > 任务名称 > 任务ID
     * 
     * @param id 任务ID
     * @return Markdown文件流
     */
    @GetMapping("/{id}/export-ai-response")
    public ResponseEntity<byte[]> exportAiResponse(@PathVariable Long id) throws java.io.UnsupportedEncodingException {
        Task task = taskService.getById(id);
        if (task == null || task.getAiResponse() == null) {
            return ResponseEntity.notFound().build();
        }
        
        byte[] content = task.getAiResponse().getBytes(StandardCharsets.UTF_8);
        
        // 文件名优先级：任务号 > 任务名称 > 任务ID
        String fileName;
        if (task.getTaskNo() != null && !task.getTaskNo().isEmpty()) {
            fileName = task.getTaskNo().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_") + ".md";
        } else if (task.getTaskName() != null && !task.getTaskName().isEmpty()) {
            fileName = task.getTaskName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]", "_") + ".md";
        } else {
            fileName = "ai_response_" + id + ".md";
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"));
        // 使用 RFC 5987 编码处理中文文件名，确保浏览器正确识别
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8")
                .replace("+", "%20");
        headers.set("Content-Disposition", 
                "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        headers.setContentLength(content.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(content);
    }

    /**
     * 测试用例生成请求参数
     */
    @lombok.Data
    public static class GenerateRequest {
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
        /** AI配置信息 */
        private AiConfig aiConfig;
    }

    /**
     * 获取任务统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<TaskStatisticsDTO> getStatistics() {
        TaskStatisticsDTO statistics = taskService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 分页查询任务列表
     *
     * @param queryRequest 查询条件
     * @return 任务列表（TaskListVO，支持多文档类型）
     */
    @PostMapping("/list")
    public Result<IPage<TaskListVO>> listTasks(@RequestBody TaskQueryRequest queryRequest) {
        IPage<TaskListVO> page = taskService.queryTaskList(queryRequest);
        return Result.success(page);
    }

    /**
     * 获取任务详情
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    @GetMapping("/detail/{id}")
    public Result<TaskDetailDTO> getTaskDetail(@PathVariable Long id) {
        TaskDetailDTO detail = taskService.getTaskDetailInfo(id);
        if (detail == null) {
            return Result.error("任务不存在");
        }
        return Result.success(detail);
    }

    /**
     * 下载任务生成的测试用例文件
     *
     * @param id 任务 ID
     * @return Markdown 文件
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadTestCase(@PathVariable Long id) {
        TaskFileDTO fileDTO = taskService.getTestCaseFile(id);
        if (fileDTO == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fileDTO.getFileName())
                .contentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"))
                .body(fileDTO.getContent());
    }
}
