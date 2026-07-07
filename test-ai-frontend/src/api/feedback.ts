import request from '../utils/request'

export function submitFeedback(data: any) {
  return request({
    url: '/api/v1/task-feedback/submit',
    method: 'post',
    data
  })
}

export function getFeedback(taskId: number) {
  return request({
    url: `/api/v1/task-feedback/${taskId}`,
    method: 'get'
  })
}
