package com.testai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.testai.dto.TaskFeedbackRequest;
import com.testai.entity.TaskFeedback;

/**
 * 任务评价服务接口
 */
public interface TaskFeedbackService extends IService<TaskFeedback> {

    /**
     * 提交评价
     *
     * @param request 评价请求
     * @return 评价结果
     */
    TaskFeedback submitFeedback(TaskFeedbackRequest request);

    /**
     * 根据任务 ID 获取评价
     *
     * @param taskId 任务 ID
     * @return 评价信息
     */
    TaskFeedback getFeedbackByTaskId(Long taskId);
}
