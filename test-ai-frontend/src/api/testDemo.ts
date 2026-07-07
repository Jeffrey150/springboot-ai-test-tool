import { get, post, put, del } from '../utils/request'
import type { TestDemo } from '../types/api'

/**
 * 测试示例 API 接口（组件化调用）
 * 参照若依框架：每个模块独立封装 API
 */

const baseUrl = '/test-demo'

/**
 * 查询测试数据列表
 * @param name 名称（可选）
 * @returns 测试数据列表
 */
export function listTestDemo(name?: string): Promise<TestDemo[]> {
  return get<TestDemo[]>(`${baseUrl}/list`, name ? { name } : undefined)
}

/**
 * 根据ID查询测试数据
 * @param id 主键ID
 * @returns 测试数据
 */
export function getTestDemo(id: number): Promise<TestDemo> {
  return get<TestDemo>(`${baseUrl}/${id}`)
}

/**
 * 新增测试数据
 * @param data 测试数据
 * @returns 结果
 */
export function addTestDemo(data: Omit<TestDemo, 'id' | 'createTime'>): Promise<void> {
  return post<void>(baseUrl, data as Record<string, unknown>)
}

/**
 * 更新测试数据
 * @param data 测试数据
 * @returns 结果
 */
export function updateTestDemo(data: TestDemo): Promise<void> {
  return put<void>(baseUrl, data as Record<string, unknown>)
}

/**
 * 删除测试数据
 * @param id 主键ID
 * @returns 结果
 */
export function deleteTestDemo(id: number): Promise<void> {
  return del<void>(`${baseUrl}/${id}`)
}
