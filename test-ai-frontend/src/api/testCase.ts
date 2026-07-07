import request from '../utils/request'

export interface TestCase {
  id: number
  taskId: number
  content: string
  createTime: string
}

export function getTestCasesByTaskId(taskId: number) {
  return request.get<TestCase[]>(`/api/v1/test-cases/task/${taskId}`)
}

/**
 * 生成测试用例
 * @param files 上传的文件列表
 */
export function generateTestCase(files: File[]) {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })

  return request({
    url: '/api/v1/test-cases/generate',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
