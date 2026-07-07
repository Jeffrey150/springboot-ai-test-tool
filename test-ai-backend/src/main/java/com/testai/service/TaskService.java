package com.testai.service;

import com.testai.common.PageResult;
import com.testai.dto.TaskCreateDTO;
import com.testai.dto.TaskFeedbackDTO;
import com.testai.dto.TaskListVO;
import com.testai.entity.Task;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.testai.dto.TaskDetailDTO;
import com.testai.dto.TaskFileDTO;
import com.testai.dto.TaskQueryRequest;
import com.testai.dto.TaskStatisticsDTO;

public interface TaskService extends IService<Task> {

    /**
     * 创建单文档任务
     * 
     * @param dto 任务创建参数
     * @return 创建的任务实体
     */
    Task createTask(TaskCreateDTO dto);

    /**
     * 创建多文档任务
     * 
     * @param dto 任务创建参数
     * @return 创建的任务实体
     */
    Task createMultiDocumentTask(TaskCreateDTO dto);

    /**
     * 根据ID查询任务详情
     * 
     * @param id 任务ID
     * @return 任务实体
     */
    Task getById(Long id);

    /**
     * 分页查询任务列表
     * 
     * @param taskStatus 任务状态筛选（可选）
     * @param documentType 文档类型筛选（可选）
     * @param startDate 开始日期筛选（可选）
     * @param endDate 结束日期筛选（可选）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    PageResult<Task> listTasks(Integer taskStatus, String documentType, String startDate,
                                String endDate, Integer pageNum, Integer pageSize);

    /**
     * 提交任务反馈
     * 
     * @param id 任务ID
     * @param dto 反馈内容
     */
    void submitFeedback(Long id, TaskFeedbackDTO dto);

    /**
     * 删除任务
     * 
     * @param id 任务ID
     */
    void deleteById(Long id);

    /**
     * 更新任务状态
     * 
     * @param id 任务ID
     * @param status 任务状态（0-待处理，1-进行中，2-已完成，3-失败）
     */
    void updateTaskStatus(Long id, Integer status);

    /**
     * 更新任务结果
     * 
     * @param id 任务ID
     * @param resultCount 生成的测试用例数量
     * @param qualityScore 质量评分
     * @param duration 耗时（秒）
     */
    void updateTaskResult(Long id, Integer resultCount, Double qualityScore, Integer duration);

    /**
     * 保存AI响应内容
     * 
     * @param id 任务ID
     * @param aiResponse AI生成的响应内容
     */
    void saveAiResponse(Long id, String aiResponse);

    /**
     * 获取任务统计信息
     *
     * @return 统计信息
     */
    TaskStatisticsDTO getStatistics();

    /**
     * 分页查询任务列表
     *
     * @param queryRequest 查询条件
     * @return 分页结果（TaskListVO，支持多文档类型）
     */
    IPage<TaskListVO> queryTaskList(TaskQueryRequest queryRequest);

    /**
     * 根据 ID 获取任务详情
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    Task getTaskById(Long id);

    /**
     * 获取任务详情信息（用于弹窗展示）
     *
     * @param id 任务 ID
     * @return 任务详情 DTO
     */
    TaskDetailDTO getTaskDetailInfo(Long id);

    /**
     * 获取任务测试用例文件内容
     *
     * @param id 任务 ID
     * @return 文件信息（文件名 + 内容字节数组），失败返回 null
     */
    TaskFileDTO getTestCaseFile(Long id);
}
