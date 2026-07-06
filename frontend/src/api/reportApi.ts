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

/** 报告相关 RTK Query API */
export const reportApi = createApi({
  reducerPath: 'reportApi',
  baseQuery: baseQueryWithRestResponse,
  tagTypes: ['Report'],
  endpoints: (build) => ({
    getReports: build.query<unknown, { page?: number; size?: number; keyword?: string }>({
      query: ({ page = 1, size = 20, keyword }) => ({
        url: 'reports',
        params: { page, size, keyword },
      }),
      providesTags: [{ type: 'Report', id: 'LIST' }],
    }),
    getReportById: build.query<unknown, number>({
      query: (id) => ({ url: `reports/${id}` }),
      providesTags: (_r, _e, id) => [{ type: 'Report', id }],
    }),
  }),
})

export const { useGetReportsQuery, useGetReportByIdQuery } = reportApi
