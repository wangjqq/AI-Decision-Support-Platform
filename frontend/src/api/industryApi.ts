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

/** 适配后端 RestResponse<T> 统一返回结构 */
const baseQueryWithRestResponse: typeof rawBaseQuery = async (args, api, extraOptions) => {
  const result = await rawBaseQuery(args, api, extraOptions)
  if (result.error) return result
  const payload = result.data as RestResponse | undefined
  if (payload && typeof payload === 'object' && 'code' in payload) {
    if (payload.code === 0) {
      return { ...result, data: payload.data as unknown as Record<string, unknown> }
    }
    return {
      error: { status: payload.code, data: { msg: payload.msg, traceId: payload.traceId } },
      data: undefined,
    } as typeof result
  }
  return result
}

/** 行业相关 RTK Query API */
export const industryApi = createApi({
  reducerPath: 'industryApi',
  baseQuery: baseQueryWithRestResponse,
  tagTypes: ['Industry'],
  endpoints: (build) => ({
    getIndustries: build.query<unknown, { page?: number; size?: number; keyword?: string }>({
      query: ({ page = 1, size = 20, keyword }) => ({
        url: 'industries',
        params: { page, size, keyword },
      }),
      providesTags: [{ type: 'Industry', id: 'LIST' }],
    }),
    getIndustryById: build.query<unknown, number>({
      query: (id) => ({ url: `industries/${id}` }),
      providesTags: (_r, _e, id) => [{ type: 'Industry', id }],
    }),
  }),
})

export const { useGetIndustriesQuery, useGetIndustryByIdQuery } = industryApi
