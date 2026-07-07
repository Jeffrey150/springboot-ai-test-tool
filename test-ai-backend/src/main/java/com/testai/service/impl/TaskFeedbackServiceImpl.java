package com.testai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testai.dto.TaskFeedbackRequest;
import com.testai.entity.Task;
import com.testai.entity.TaskFeedback;
import com.testai.mapper.TaskFeedbackMapper;
import com.testai.service.TaskFeedbackService;
import com.testai.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 任务评价服务实现
 */
@Service
public class TaskFeedbackServiceImpl extends ServiceImpl<TaskFeedbackMapper, TaskFeedback> implements TaskFeedbackService {

    @Autowired
    private TaskFeedbackMapper taskFeedbackMapper;

    @Autowired
    private TaskService taskService;

    /**
     * 提交评价
     *
     * @param request 评价请求
     * @return 评价结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskFeedback submitFeedback(TaskFeedbackRequest request) {
        // 检查是否已经评价过
        TaskFeedback existingFeedback = taskFeedbackMapper.selectByTaskId(request.getTaskId());
        if (existingFeedback != null) {
            throw new RuntimeException("该任务已评价过，不能重复评价");
        }

        // 检查任务是否存在
        Task task = taskService.getById(request.getTaskId());
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 创建评价记录
        TaskFeedback feedback = new TaskFeedback();
        feedback.setTaskId(request.getTaskId());
        feedback.setQualityScore(request.getQualityScore());
        feedback.setAdoptionRate(request.getAdoptionRate());
        feedback.setSuggestion(request.getSuggestion());

        // 保存评价
        taskFeedbackMapper.insert(feedback);

        // 更新任务状态为已评价
        task.setTaskStatus(4);
        taskService.updateById(task);

        return feedback;
    }

    /**
     * 根据任务 ID 获取评价
     *
     * @param taskId 任务 ID
     * @return 评价信息
     */
    @Override
    public TaskFeedback getFeedbackByTaskId(Long taskId) {
        return taskFeedbackMapper.selectByTaskId(taskId);
    }
}
