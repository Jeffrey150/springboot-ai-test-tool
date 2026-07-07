import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

service.interceptors.request.use(
  (config) => {
    if (config.data instanceof FormData) {
      delete config.headers['Content-Type']
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    if (response.config.responseType === 'blob') {
      const contentType = response.headers['content-type']
      if (contentType && contentType.includes('application/json')) {
        return response.data.text().then((text: string) => {
          const json = JSON.parse(text)
          if (json.code === 200) {
            return response.data
          }
          ElMessage.error(json.msg || '请求失败')
          return Promise.reject(new Error(json.msg || '请求失败'))
        })
      }
      return response.data
    }
    
    const { data } = response
    if (data.code === 200) {
      return data.data
    }
    ElMessage.error(data.msg || '请求失败')
    return Promise.reject(new Error(data.msg || '请求失败'))
  },
  (error) => {
    let message = '网络错误'
    if (error.response) {
      const status = error.response.status
      if (error.response.config.responseType === 'blob' && error.response.data instanceof Blob) {
        return error.response.data.text().then((text: string) => {
          try {
            const json = JSON.parse(text)
            message = json.msg || `请求失败: ${status}`
          } catch {
            switch (status) {
              case 400:
                message = '请求参数错误'
                break
              case 401:
                message = '未授权，请重新登录'
                break
              case 403:
                message = '拒绝访问'
                break
              case 404:
                message = '请求地址不存在'
                break
              case 500:
                message = '服务器内部错误'
                break
              default:
                message = `请求失败: ${status}`
            }
          }
          ElMessage.error(message)
          return Promise.reject(error)
        })
      }
      switch (status) {
        case 400:
          message = '请求参数错误'
          break
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        default:
          message = `请求失败: ${status}`
      }
    } else if (error.message.includes('timeout')) {
      message = '请求超时'
    } else if (error.message.includes('Network Error')) {
      message = '网络连接失败'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export function request<T>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config) as Promise<T>
}

export function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  return request<T>({
    method: 'get',
    url,
    params
  })
}

export function post<T>(url: string, data?: Record<string, unknown>): Promise<T> {
  return request<T>({
    method: 'post',
    url,
    data
  })
}

export function put<T>(url: string, data?: Record<string, unknown>): Promise<T> {
  return request<T>({
    method: 'put',
    url,
    data
  })
}

export function del<T>(url: string): Promise<T> {
  return request<T>({
    method: 'delete',
    url
  })
}

export default service
