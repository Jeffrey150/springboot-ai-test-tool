import request from '@/utils/request'

/**
 * 任务实体接口
 */
export interface Task {
  id: number                    // 任务ID
  documentId: number            // 关联文档ID（单文档模式）
  documentIds?: string          // 关联文档ID列表（多文档模式）
  documentType?: string         // 文档类型
  taskName: string              // 任务名称
  taskNo?: string               // 任务号
  taskStatus: number            // 任务状态（0-待处理，1-进行中，2-已完成，3-失败）
  templateId: number            // 模板ID
  config: string                // 配置信息（JSON格式）
  startTime: string             // 开始时间
  endTime: string               // 结束时间
  duration: number              // 耗时（秒）
  resultCount: number           // 生成的测试用例数量
  qualityScore: number          // 质量评分
  feedback: string              // 反馈内容
  aiResponse?: string           // AI生成的响应内容
  createTime: string            // 创建时间
  updateTime: string            // 更新时间
}

/**
 * 任务创建请求接口
 */
export interface TaskCreateRequest {
  documentId: number            // 文档ID
  taskName: string              // 任务名称
  templateId?: number           // 模板ID（可选）
  config?: string               // 配置信息（可选）
}

/**
 * 任务反馈请求接口
 */
export interface TaskFeedbackRequest {
  rating?: number               // 评分（可选）
  feedback?: string             // 反馈内容（可选）
  adoptionRate?: number         // 采用率（可选）
}

export interface AiConfig {
  platform: string              // AI平台（aliyun/openai/gemini）
  apiUrl: string                // API地址
  model: string                 // 模型名称
  apiKey: string                // API密钥
}

/**
 * 测试用例生成请求接口
 */
export interface GenerateTestCaseRequest {
  documentId?: number           // 单文档ID（单文档模式，可选）
  documentIds?: number[]        // 多文档ID列表（多文档模式，可选）
  taskName: string              // 任务名称
  taskNo: string                // 任务号
  templateId?: number           // 模板ID（可选）
  aiConfig: AiConfig            // AI配置信息
}

export function createTask(data: TaskCreateRequest) {
  return request.post<Task>('/api/tasks/create', data)
}

export function getTask(id: number) {
  return request.get<Task>(`/api/v1/tasks/${id}`)
}

export function deleteTask(id: number) {
  return request.delete(`/api/tasks/${id}`)
}
/**
 * 生成测试用例
 * 
 * @param data 生成请求参数
 * @returns 返回任务ID
 */
export function exportAiResponse(id: number) {
  return request.get(`/api/tasks/${id}/export-ai-response`, {
    responseType: 'blob'
  })
}

export function generateTestCases(data: GenerateTestCaseRequest) {
  return request.post<{ taskId: number }>('/api/v1/tasks/generate', data)
}

export function getTaskStatistics() {
  return request.get('/api/v1/tasks/statistics')
}

export function getTaskList(data: any) {
  return request.post('/api/v1/tasks/list', data)
}

export function getTaskDetail(id: number) {
  return request.get(`/api/v1/tasks/detail/${id}`)
}

export function downloadTestCase(id: number) {
  return request.get(`/api/v1/tasks/${id}/download`, {
    responseType: 'blob'
  })
}

export function submitTaskFeedback(id: number, feedback: string) {
  return request.post(`/api/v1/tasks/${id}/feedback`, { feedback })
}
