import request from '@/utils/request'

/**
 * 任务统计响应类型
 */
export interface TaskStatistics {
  /** 已完成任务数量 */
  completedTaskCount: number
  /** 已评价任务数量 */
  evaluatedTaskCount: number
  /** 已完成任务的用例总量 */
  completedTestCaseCount: number
  /** 已评价任务的用例总量 */
  evaluatedTestCaseCount: number
  /** 已完成任务的总执行时长（秒） */
  completedTotalDuration: number
  /** 已评价任务的总执行时长（秒） */
  evaluatedTotalDuration: number
}

/**
 * 获取任务统计数据
 * @param timeRange 时间范围：month-本月，week-本周，all-全部
 */
export function getTaskStatistics(timeRange: string = 'month') {
  return request.get<TaskStatistics>('/api/v1/task-statistics/statistics', {
    params: { timeRange }
  })
}
