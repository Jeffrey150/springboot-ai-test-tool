package com.testai.service.impl;

import com.testai.dto.TaskStatisticsVO;
import com.testai.mapper.TaskMapper;
import com.testai.service.TaskStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 任务统计服务实现类
 */
@Slf4j
@Service
public class TaskStatisticsServiceImpl implements TaskStatisticsService {

    private final TaskMapper taskMapper;

    public TaskStatisticsServiceImpl(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public TaskStatisticsVO getStatistics(String timeRange) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = LocalDateTime.now();

        // 根据时间范围计算起始时间
        switch (timeRange) {
            case "week":
                // 本周：从本周一0点开始
                startTime = endTime.minusDays(endTime.getDayOfWeek().getValue() - 1)
                        .toLocalDate().atStartOfDay();
                break;
            case "month":
                // 本月：从本月1号0点开始
                startTime = endTime.withDayOfMonth(1).toLocalDate().atStartOfDay();
                break;
            case "all":
            default:
                // 全部：不设置起始时间，查询所有数据
                startTime = null;
                break;
        }

        log.info("查询任务统计数据，时间范围：{}，起始时间：{}，结束时间：{}", timeRange, startTime, endTime);

        // 调用Mapper获取统计数据
        Map<String, Object> stats = taskMapper.getStatisticsByTimeRange(startTime, endTime);

        // 构建返回结果
        TaskStatisticsVO vo = new TaskStatisticsVO();
        vo.setCompletedTaskCount(getLongValue(stats, "completedCount"));
        vo.setEvaluatedTaskCount(getLongValue(stats, "evaluatedCount"));
        vo.setCompletedTestCaseCount(getLongValue(stats, "completedCaseCount"));
        vo.setEvaluatedTestCaseCount(getLongValue(stats, "evaluatedCaseCount"));
        vo.setCompletedTotalDuration(getLongValue(stats, "completedDuration"));
        vo.setEvaluatedTotalDuration(getLongValue(stats, "evaluatedDuration"));

        return vo;
    }

    /**
     * 从Map中安全获取Long值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
}
