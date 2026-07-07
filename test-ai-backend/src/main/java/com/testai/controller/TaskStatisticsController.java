package com.testai.controller;

import com.testai.common.Result;
import com.testai.dto.TaskStatisticsVO;
import com.testai.service.TaskStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 任务统计控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/task-statistics")
public class TaskStatisticsController {

    private final TaskStatisticsService taskStatisticsService;

    public TaskStatisticsController(TaskStatisticsService taskStatisticsService) {
        this.taskStatisticsService = taskStatisticsService;
    }

    /**
     * 获取任务统计数据
     *
     * @param timeRange 时间范围：month-本月，week-本周，all-全部，默认month
     * @return 任务统计结果
     */
    @GetMapping("/statistics")
    public Result<TaskStatisticsVO> getStatistics(
            @RequestParam(defaultValue = "month") String timeRange) {
        log.info("获取任务统计数据，时间范围：{}", timeRange);
        TaskStatisticsVO statistics = taskStatisticsService.getStatistics(timeRange);
        return Result.success(statistics);
    }
}
