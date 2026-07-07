package com.testai.service;

import com.testai.dto.TaskStatisticsVO;

/**
 * 任务统计服务接口
 */
public interface TaskStatisticsService {

    /**
     * 获取任务统计数据
     *
     * @param timeRange 时间范围：month-本月，week-本周，all-全部
     * @return 任务统计结果
     */
    TaskStatisticsVO getStatistics(String timeRange);
}
