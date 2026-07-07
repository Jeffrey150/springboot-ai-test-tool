/**
 * 统一响应结果类型
 */
export interface Result<T> {
  code: number
  msg: string
  data: T
}

/**
 * 测试示例数据类型
 */
export interface TestDemo {
  id: number
  name: string
  value: string
  description: string
  createTime: string
}
