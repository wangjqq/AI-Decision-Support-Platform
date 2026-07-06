import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import type { RestResponse } from './http'

const TOKEN_KEY = 'aidsp_token'

const rawBaseQuery = fetchBaseQuery({
  baseUrl: '/api/v1/',
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

/**
 * 为 RTK Query 适配后端 RestResponse<T> 包装结构：
 * - 当返回 code === 0 时，将 data 解包给调用方
 * - 非 0 时抛错，由 store 的 matcher 统一弹出 message
 */
const baseQueryWithRestResponse: typeof rawBaseQuery = async (args, api, extraOptions) => {
  const result = await rawBaseQuery(args, api, extraOptions)
  if (result.error) {
    return result
  }
  const payload = result.data as RestResponse | undefined
  if (payload && typeof payload === 'object' && 'code' in payload) {
    if (payload.code === 0) {
      return { ...result, data: payload.data as unknown as Record<string, unknown> }
    }
    return {
      error: {
        status: payload.code,
        data: { msg: payload.msg, traceId: payload.traceId },
      },
      data: undefined,
    } as typeof result
  }
  return result
}

/** 公司相关 RTK Query API */
export const companyApi = createApi({
  reducerPath: 'companyApi',
  baseQuery: baseQueryWithRestResponse,
  tagTypes: ['Company'],
  endpoints: (build) => ({
    // 占位：获取公司分页列表（具体参数待后端契约稳定后细化）
    getCompanies: build.query<unknown, { page?: number; size?: number; keyword?: string }>({
      query: ({ page = 1, size = 20, keyword }) => ({
        url: 'companies',
        params: { page, size, keyword },
      }),
      providesTags: (result) =>
        result
          ? [
              ...((result as { list?: { id: number }[] }).list?.map((c) => ({ type: 'Company' as const, id: c.id })) ??
                []),
              { type: 'Company' as const, id: 'LIST' },
            ]
          : [{ type: 'Company' as const, id: 'LIST' }],
    }),
    // 占位：按 ID 获取公司详情
    getCompanyById: build.query<unknown, number>({
      query: (id) => ({ url: `companies/${id}` }),
      providesTags: (_result, _error, id) => [{ type: 'Company' as const, id }],
    }),
  }),
})

export const { useGetCompaniesQuery, useGetCompanyByIdQuery } = companyApi
