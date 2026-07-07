package com.testai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testai.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
 * 任务数据访问层
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
      /**
     * 获取任务统计信息
     *
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN task_status = 2 THEN 1 ELSE 0 END) as successCount, " +
            "SUM(CASE WHEN task_status = 3 THEN 1 ELSE 0 END) as failedCount, " +
            "SUM(CASE WHEN task_status = 1 THEN 1 ELSE 0 END) as processingCount " +
            "FROM task")
    Map<String, Object> getTaskStatistics();

    /**
     * 获取任务统计数据（支持时间范围过滤）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    @Select("<script>" +
            "SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN task_status IN (2, 4) THEN 1 ELSE 0 END) as completedCount, " +
            "SUM(CASE WHEN task_status IN (2, 4) THEN result_count ELSE 0 END) as completedCaseCount, " +
            "SUM(CASE WHEN task_status IN (2, 4) THEN duration ELSE 0 END) as completedDuration, " +
            "SUM(CASE WHEN task_status = 4 THEN 1 ELSE 0 END) as evaluatedCount, " +
            "SUM(CASE WHEN task_status = 4 THEN result_count ELSE 0 END) as evaluatedCaseCount, " +
            "SUM(CASE WHEN task_status = 4 THEN duration ELSE 0 END) as evaluatedDuration " +
            "FROM task " +
            "<where>" +
            "<if test='startTime != null'> AND create_time &gt;= #{startTime} </if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime} </if>" +
            "</where>" +
            "</script>")
    Map<String, Object> getStatisticsByTimeRange(@Param("startTime") java.time.LocalDateTime startTime,
                                                   @Param("endTime") java.time.LocalDateTime endTime);
}
