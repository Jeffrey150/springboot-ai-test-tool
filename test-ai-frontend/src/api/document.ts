import request from '@/utils/request'

/**
 * 文档实体接口
 */
export interface Document {
  id: number              // 文档ID
  fileName: string        // 文件名
  filePath: string        // 文件存储路径
  fileType: string        // 文件类型（扩展名）
  fileSize: number        // 文件大小（字节）
  documentType: string    // 文档类型（如 REQUIREMENT、DESIGN 等）
  status: number          // 状态（0-无效，1-有效）
  createTime: string      // 创建时间
  updateTime: string      // 更新时间
}

/**
 * 上传单个文档
 * 
 * @param file 文件
 * @param documentType 文档类型，默认为 REQUIREMENT
 * @returns 上传的文档实体
 */
export function uploadDocument(file: File, documentType: string = 'REQUIREMENT') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('documentType', documentType)
  return request.post<Document>('/documents/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 批量上传文档，支持为每个文档指定不同类型
 * 
 * @param files 文件列表
 * @param documentTypes 文档类型列表，与文件列表一一对应
 * @returns 上传的文档列表
 */
export function uploadDocumentsWithTypes(files: File[], documentTypes: string[]) {
  const formData = new FormData()
  files.forEach((file, index) => {
    formData.append('file', file)
    formData.append('documentTypes', documentTypes[index])
  })
  return request.post<Document[]>('/api/documents/upload-with-types', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 根据ID查询文档详情
 * 
 * @param id 文档ID
 * @returns 文档实体
 */
export function getDocument(id: number) {
  return request.get<Document>(`/documents/${id}`)
}

/**
 * 删除文档
 * 
 * @param id 文档ID
 * @returns 操作结果
 */
export function deleteDocument(id: number) {
  return request.delete(`/documents/${id}`)
}

/**
 * 查询文档列表
 * 
 * @param documentType 文档类型筛选（可选）
 * @returns 文档列表
 */
export function getDocumentList(documentType?: string) {
  return request.get<Document[]>('/documents/list', {
    params: { documentType }
  })
}

/**
 * 获取文档内容
 * 
 * @param id 文档ID
 * @returns 文档内容文本
 */
export function getDocumentContent(id: number) {
  return request.get<string>(`/documents/${id}/content`)
}
