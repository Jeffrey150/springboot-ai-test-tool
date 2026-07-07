package com.testai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testai.entity.TaskFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 任务评价 Mapper 接口
 */
@Mapper
public interface TaskFeedbackMapper extends BaseMapper<TaskFeedback> {

    /**
     * 根据任务 ID 查询评价
     *
     * @param taskId 任务 ID
     * @return 评价信息
     */
    @Select("SELECT * FROM task_feedback WHERE task_id = #{taskId}")
    TaskFeedback selectByTaskId(Long taskId);
}
