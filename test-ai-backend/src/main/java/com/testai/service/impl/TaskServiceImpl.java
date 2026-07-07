package com.testai.service.impl;

import com.testai.dto.TaskCreateDTO;
import com.testai.dto.TaskFeedbackDTO;
import com.testai.dto.TaskListVO;
import com.testai.entity.Document;
import com.testai.entity.Task;
import com.testai.mapper.DocumentMapper;
import com.testai.mapper.TaskMapper;
import com.testai.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.testai.common.PageResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.testai.dto.TaskDetailDTO;
import com.testai.dto.TaskFileDTO;
import com.testai.dto.TaskQueryRequest;
import com.testai.dto.TaskStatisticsDTO;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 任务服务实现类
 * 实现任务的创建、查询、更新、删除等操作
 */
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task> implements TaskService {

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private DocumentMapper documentMapper;

    /**
     * 创建单文档任务
     * 
     * @param dto 任务创建参数
     * @return 创建的任务实体
     */
    @Override
    public Task createTask(TaskCreateDTO dto) {
        Document document = documentMapper.selectById(dto.getDocumentId());
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        Task task = new Task();
        task.setDocumentId(dto.getDocumentId());
        task.setDocumentType(document.getDocumentType());
        // 优先使用任务号作为任务名称，如果没有任务号则使用传入的任务名称
        task.setTaskName(dto.getTaskNo() != null && !dto.getTaskNo().isEmpty() ? dto.getTaskNo() : dto.getTaskName());
        task.setTaskNo(dto.getTaskNo());
        task.setTemplateId(dto.getTemplateId());
        task.setConfig(dto.getConfig());
        task.setTaskStatus(0);
        task.setStartTime(LocalDateTime.now());

        taskMapper.insert(task);
        return task;
    }

    /**
     * 创建多文档任务
     * 
     * @param dto 任务创建参数
     * @return 创建的任务实体
     */
    @Override
    public Task createMultiDocumentTask(TaskCreateDTO dto) {
        if (dto.getDocumentIds() == null || dto.getDocumentIds().isEmpty()) {
            throw new RuntimeException("文档ID列表不能为空");
        }

        System.out.println("接收到的文档ID列表: " + dto.getDocumentIds());
        
        Long firstDocId = dto.getDocumentIds().get(0);
        System.out.println("第一个文档ID: " + firstDocId);
        
        Document firstDocument = documentMapper.selectById(firstDocId);
        System.out.println("查询结果: " + (firstDocument != null ? "找到文档: " + firstDocument.getFileName() : "未找到文档"));
        
        if (firstDocument == null) {
            throw new RuntimeException("文档不存在");
        }

        Task task = new Task();
        task.setDocumentId(dto.getDocumentIds().get(0));
        task.setDocumentIds(String.join(",", dto.getDocumentIds().stream().map(String::valueOf).toArray(String[]::new)));
        task.setDocumentType(firstDocument.getDocumentType());
        // 优先使用任务号作为任务名称，如果没有任务号则使用传入的任务名称
        task.setTaskName(dto.getTaskNo() != null && !dto.getTaskNo().isEmpty() ? dto.getTaskNo() : dto.getTaskName());
        task.setTaskNo(dto.getTaskNo());
        task.setTemplateId(dto.getTemplateId());
        task.setConfig(dto.getConfig());
        task.setTaskStatus(0);
        task.setStartTime(LocalDateTime.now());

        taskMapper.insert(task);
        return task;
    }

    /**
     * 根据ID查询任务详情
     * 
     * @param id 任务ID
     * @return 任务实体
     */
    @Override
    public Task getById(Long id) {
        return taskMapper.selectById(id);
    }

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
    @Override
    public PageResult<Task> listTasks(Integer taskStatus, String documentType, String startDate,
                                String endDate, Integer pageNum, Integer pageSize) {
        Page<Task> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Task> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (taskStatus != null) {
            wrapper.eq("task_status", taskStatus);
        }
        if (documentType != null && !documentType.isEmpty()) {
            wrapper.eq("document_type", documentType);
        }
        wrapper.orderByDesc("create_time");
        IPage<Task> result = taskMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
    }


    /**
     * 删除任务
     * 
     * @param id 任务ID
     */
    @Override
    public void deleteById(Long id) {
        taskMapper.deleteById(id);
    }

    /**
     * 更新任务状态
     * 
     * @param id 任务ID
     * @param status 任务状态（0-待处理，1-进行中，2-已完成，3-失败）
     */
    @Override
    public void updateTaskStatus(Long id, Integer status) {
        Task task = taskMapper.selectById(id);
        if (task != null) {
            task.setTaskStatus(status);
            if (status == 2) {
                task.setEndTime(LocalDateTime.now());
            }
            taskMapper.updateById(task);
        }
    }

    /**
     * 更新任务结果
     * 
     * @param id 任务ID
     * @param resultCount 生成的测试用例数量
     * @param qualityScore 质量评分
     * @param duration 耗时（秒）
     */
    @Override
    public void updateTaskResult(Long id, Integer resultCount, Double qualityScore, Integer duration) {
        Task task = taskMapper.selectById(id);
        if (task != null) {
            task.setResultCount(resultCount);
            task.setQualityScore(qualityScore);
            task.setDuration(duration);
            task.setTaskStatus(2);
            task.setEndTime(LocalDateTime.now());
            taskMapper.updateById(task);
        }
    }
public TaskStatisticsDTO getStatistics() {
        Map<String, Object> stats = baseMapper.getTaskStatistics();
        
        TaskStatisticsDTO dto = new TaskStatisticsDTO();
        dto.setTotalCount(getLongValue(stats.get("totalCount")));
        dto.setSuccessCount(getLongValue(stats.get("successCount")));
        dto.setFailedCount(getLongValue(stats.get("failedCount")));
        dto.setProcessingCount(getLongValue(stats.get("processingCount")));
        
        return dto;
    }

    /**
     * 分页查询任务列表
     *
     * @param queryRequest 查询条件
     * @return 分页结果（TaskListVO）
     */
    @Override
    public IPage<TaskListVO> queryTaskList(TaskQueryRequest queryRequest) {
        Page<Task> page = new Page<>(queryRequest.getCurrent(), queryRequest.getSize());

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();

        // 任务名称模糊查询
        if (StringUtils.hasText(queryRequest.getTaskName())) {
            wrapper.like(Task::getTaskName, queryRequest.getTaskName());
        }

        // 文档类型（单类型筛选仅在多文档模式下作为"包含"条件使用）
        if (StringUtils.hasText(queryRequest.getDocumentType())) {
            // 同时匹配单类型字段和documentIds列表中包含的类型
            wrapper.and(w -> w.eq(Task::getDocumentType, queryRequest.getDocumentType())
                    .or().like(Task::getDocumentIds, queryRequest.getDocumentType()));
        }

        // 任务状态
        if (queryRequest.getTaskStatus() != null) {
            wrapper.eq(Task::getTaskStatus, queryRequest.getTaskStatus());
        }

        // 创建时间范围
        if (StringUtils.hasText(queryRequest.getStartTime())) {
            wrapper.ge(Task::getCreateTime,
                LocalDateTime.parse(queryRequest.getStartTime(), DateTimeFormatter.ISO_DATE_TIME));
        }
        if (StringUtils.hasText(queryRequest.getEndTime())) {
            wrapper.le(Task::getCreateTime,
                LocalDateTime.parse(queryRequest.getEndTime(), DateTimeFormatter.ISO_DATE_TIME));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Task::getCreateTime);

        IPage<Task> result = baseMapper.selectPage(page, wrapper);

        // 转换为 VO，包含多文档类型列表
        return result.convert(this::convertToListVO);
    }

    /**
     * 将 Task 实体转换为 TaskListVO
     * 实时查询关联文档，提取所有文档类型（去重）
     *
     * @param task 任务实体
     * @return 列表展示 VO
     */
    private TaskListVO convertToListVO(Task task) {
        if (task == null) {
            return null;
        }
        TaskListVO vo = new TaskListVO();
        vo.setId(task.getId());
        vo.setTaskName(task.getTaskName());
        vo.setTaskNo(task.getTaskNo());
        vo.setTaskStatus(task.getTaskStatus());
        vo.setResultCount(task.getResultCount());
        vo.setQualityScore(task.getQualityScore());
        vo.setDuration(task.getDuration());
        vo.setCreateTime(task.getCreateTime());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());
        vo.setDocumentId(task.getDocumentId());
        vo.setDocumentIds(task.getDocumentIds());

        // 提取所有关联文档的类型（去重）
        vo.setDocumentTypes(resolveDocumentTypes(task));

        return vo;
    }

    /**
     * 解析任务关联的所有文档类型
     * 支持单文档（documentId）和多文档（documentIds，逗号分隔）两种模式
     *
     * @param task 任务实体
     * @return 文档类型列表（去重且保持顺序）
     */
    private List<String> resolveDocumentTypes(Task task) {
        LinkedHashSet<String> typeSet = new LinkedHashSet<>();

        // 1. 多文档模式：遍历 documentIds 查询每个文档的类型
        if (StringUtils.hasText(task.getDocumentIds())) {
            String[] idArray = task.getDocumentIds().split(",");
            for (String docIdStr : idArray) {
                try {
                    Long docId = Long.parseLong(docIdStr.trim());
                    Document doc = documentMapper.selectById(docId);
                    if (doc != null && StringUtils.hasText(doc.getDocumentType())) {
                        typeSet.add(doc.getDocumentType());
                    }
                } catch (NumberFormatException ignored) {
                    // 跳过非法的 ID
                }
            }
        }

        // 2. 单文档模式：查询 documentId 对应的类型
        // （多文档模式下，documentId 字段存的是第一个文档的ID，可能已在上面处理过）
        if (task.getDocumentId() != null) {
            Document doc = documentMapper.selectById(task.getDocumentId());
            if (doc != null && StringUtils.hasText(doc.getDocumentType())) {
                typeSet.add(doc.getDocumentType());
            }
        }

        // 3. 兜底：如果都没有查到，尝试使用 task.documentType 字段
        if (typeSet.isEmpty() && StringUtils.hasText(task.getDocumentType())) {
            typeSet.add(task.getDocumentType());
        }

        return new ArrayList<>(typeSet);
    }

    /**
     * 根据 ID 获取任务详情
     *
     * @param id 任务 ID
     * @return 任务详情
     */
    @Override
    public Task getTaskById(Long id) {
        return getById(id);
    }

    /**
     * 获取任务详情信息（用于弹窗展示）
     *
     * @param id 任务 ID
     * @return 任务详情 DTO
     */
    @Override
    public TaskDetailDTO getTaskDetailInfo(Long id) {
        Task task = getById(id);
        if (task == null) {
            return null;
        }

        TaskDetailDTO dto = new TaskDetailDTO();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setDocumentType(task.getDocumentType());
        dto.setTaskStatus(task.getTaskStatus());
        dto.setResultCount(task.getResultCount());
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        dto.setDuration(task.getDuration());
        dto.setQualityScore(task.getQualityScore() != null ? BigDecimal.valueOf(task.getQualityScore()) : null);
        dto.setAiResponse(task.getAiResponse());
        dto.setCreateTime(task.getCreateTime());

        return dto;
    }

    /**
     * 获取任务测试用例文件内容
     *
     * @param id 任务 ID
     * @return 文件信息（文件名 + 内容字节数组），失败返回 null
     */
    @Override
    public TaskFileDTO getTestCaseFile(Long id) {
        Task task = getById(id);
        if (task == null) {
            return null;
        }

        if (task.getAiResponse() == null || task.getAiResponse().isEmpty()) {
            return null;
        }

        String fileName = task.getTaskName();
        if (fileName != null && fileName.contains(".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        fileName = fileName + ".md";

        // URL 编码文件名，支持中文
        String encodedFileName;
        try {
            encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encodedFileName = fileName;
        }

        byte[] content = task.getAiResponse().getBytes(StandardCharsets.UTF_8);

        return new TaskFileDTO(encodedFileName, content);
    }

    /**
     * 转换 Object 为 Long
     */
    private Long getLongValue(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    @Override
    public void submitFeedback(Long id, TaskFeedbackDTO dto) {
        Task task = taskMapper.selectById(id);
        if (task != null) {
            task.setFeedback(dto.getFeedback());
            taskMapper.updateById(task);
        }
    }

    /**
     * 保存AI响应内容
     * 
     * @param id 任务ID
     * @param aiResponse AI生成的响应内容
     */
    @Override
    public void saveAiResponse(Long id, String aiResponse) {
        Task task = taskMapper.selectById(id);
        if (task != null) {
            task.setAiResponse(aiResponse);
            taskMapper.updateById(task);
        }
    }
}
