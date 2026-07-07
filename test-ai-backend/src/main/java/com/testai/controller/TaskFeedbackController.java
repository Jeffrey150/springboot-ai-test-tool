package com.testai.controller;

import com.testai.common.Result;
import com.testai.dto.TaskFeedbackRequest;
import com.testai.entity.TaskFeedback;
import com.testai.service.TaskFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 任务评价控制器
 */
@RestController
@RequestMapping("/api/v1/task-feedback")
public class TaskFeedbackController {

    @Autowired
    private TaskFeedbackService taskFeedbackService;

    /**
     * 提交评价
     *
     * @param request 评价请求
     * @return 评价结果
     */
    @PostMapping("/submit")
    public Result<TaskFeedback> submitFeedback(@RequestBody @Validated TaskFeedbackRequest request) {
        TaskFeedback feedback = taskFeedbackService.submitFeedback(request);
        return Result.success(feedback);
    }

    /**
     * 获取任务评价
     *
     * @param taskId 任务 ID
     * @return 评价信息
     */
    @GetMapping("/{taskId}")
    public Result<TaskFeedback> getFeedback(@PathVariable Long taskId) {
        TaskFeedback feedback = taskFeedbackService.getFeedbackByTaskId(taskId);
        if (feedback == null) {
            return Result.error("暂无评价");
        }
        return Result.success(feedback);
    }
}
