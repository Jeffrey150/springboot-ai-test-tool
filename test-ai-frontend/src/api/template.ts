import request from '@/utils/request'

/**
 * 模板实体接口
 */
export interface Template {
  id: number              // 模板ID
  templateName: string    // 模板名称
  templateType: string    // 模板类型
  content: string         // 模板内容
  config: string          // 配置信息（JSON格式）
  status: number          // 状态（0-禁用，1-启用）
  createTime: string      // 创建时间
  updateTime: string      // 更新时间
}

/**
 * 创建模板
 * 
 * @param data 模板数据
 * @returns 创建的模板实体
 */
export function createTemplate(data: Partial<Template>) {
  return request.post<Template>('/api/templates/create', data)
}

/**
 * 根据ID查询模板详情
 * 
 * @param id 模板ID
 * @returns 模板实体
 */
export function getTemplate(id: number) {
  return request.get<Template>(`/api/templates/${id}`)
}

/**
 * 查询模板列表
 * 
 * @param templateType 模板类型筛选（可选）
 * @returns 模板列表
 */
export function getTemplateList(templateType?: string) {
  return request.get<Template[]>('/api/templates/list', {
    params: { templateType }
  })
}

/**
 * 更新模板
 * 
 * @param id 模板ID
 * @param data 更新的模板数据
 * @returns 更新后的模板实体
 */
export function updateTemplate(id: number, data: Partial<Template>) {
  return request.put<Template>(`/api/templates/${id}`, data)
}

/**
 * 删除模板
 * 
 * @param id 模板ID
 * @returns 操作结果
 */
export function deleteTemplate(id: number) {
  return request.delete(`/api/templates/${id}`)
}
