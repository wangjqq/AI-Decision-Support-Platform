import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'

/** 与后端 RestResponse<T> 对齐的响应结构（见 docs/api-spec.md §2） */
export interface RestResponse<T = unknown> {
  code: number
  msg: string
  data: T
  traceId?: string
  timestamp?: number
}

/** 业务异常：用于在拦截器抛出统一错误，由调用方决定如何处理 */
export class ApiError extends Error {
  code: number
  constructor(code: number, msg: string) {
    super(msg)
    this.code = code
    this.name = 'ApiError'
  }
}

const TOKEN_KEY = 'aidsp_token'

/** 从 localStorage 读取登录 token */
export const getToken = (): string | null => {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

/** 写入 token（如登录成功后） */
export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

/** 清除 token（登出时） */
export const clearToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 统一 axios 实例
 * - baseURL 留空：开发期走 vite proxy (/api -> http://localhost:8013)
 * - 请求拦截器：注入 Authorization: Bearer <token>
 * - 响应拦截器：解包 RestResponse，code === 0 返回 data；非 0 用 message.error 提示后 throw
 */
const http: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
})

http.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error),
)

http.interceptors.response.use(
  (response) => {
    // 后端统一返回 RestResponse<T>，data 字段为业务体
    const payload = response.data as RestResponse | undefined
    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code === 0) {
        // 成功：直接返回 data，外层调用方拿到的是业务对象
        return payload.data as unknown as typeof response.data
      }
      // 业务失败：弹窗提示并抛出 ApiError
      const errMsg = payload.msg || '业务请求失败'
      message.error(errMsg)
      return Promise.reject(new ApiError(payload.code, errMsg))
    }
    // 非标准响应（如下载/文件流），原样返回
    return response.data
  },
  (error: AxiosError<RestResponse>) => {
    // 网络层 / HTTP 状态码错误统一处理
    if (error.response) {
      const data = error.response.data
      const msg = (data && (data.msg || data.message)) || `请求失败 (${error.response.status})`
      message.error(msg)
      return Promise.reject(new ApiError(error.response.status, msg))
    }
    if (error.request) {
      message.error('网络异常，请检查后端服务是否启动')
      return Promise.reject(new ApiError(-1, '网络异常'))
    }
    message.error(error.message || '请求出错')
    return Promise.reject(new ApiError(-2, error.message || '请求出错'))
  },
)

export default http
