import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'
import { fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import type { FetchArgs } from '@reduxjs/toolkit/query'

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
      const msg = (data && data.msg) || `请求失败 (${error.response.status})`
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

/**
 * 构造一个适配后端 RestResponse<T> 包装结构的 RTK Query baseQuery。
 *
 * 使用场景：所有 RTK Query slice 共享同一套：
 *   1) baseUrl（一般传 '/api/v1/'，由 vite proxy 转发到后端）
 *   2) Authorization: Bearer <token> 注入（复用 TOKEN_KEY）
 *   3) 当返回 code === 0 时解包 data；非 0 返回 error（不含 message.error，
 *      错误提示交给调用方或全局拦截器统一处理）
 *
 * 之所以抽到 http.ts：避免在每个 *Api.ts 中重复 rawBaseQuery + 包装函数。
 */
export const createRestResponseBaseQuery = (baseUrl: string) => {
  const rawBaseQuery = fetchBaseQuery({
    baseUrl,
    prepareHeaders: (headers) => {
      try {
        const token = localStorage.getItem(TOKEN_KEY)
        if (token) {
          headers.set('Authorization', `Bearer ${token}`)
        }
      } catch {
        /* ignore */
      }
      headers.set('Content-Type', 'application/json;charset=utf-8')
      return headers
    },
  })

  const baseQueryWithRestResponse: typeof rawBaseQuery = async (args: string | FetchArgs, api, extraOptions) => {
    const result = await rawBaseQuery(args, api, extraOptions)
    if (result.error) {
      return result
    }
    const payload = result.data as RestResponse | undefined
    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code === 0) {
        return { ...result, data: payload.data as unknown as Record<string, unknown> }
      }
      // 业务失败：构造 RTK Query 标准 error 返回；非 0 code 透传给调用方
      // 错误提示由上层 message.error 统一处理（避免 RTK Query 中重复 toast）
      return {
        error: {
          status: payload.code,
          data: { msg: payload.msg, traceId: payload.traceId },
        },
        data: undefined,
      } as unknown as typeof result
    }
    return result
  }

  return baseQueryWithRestResponse
}
