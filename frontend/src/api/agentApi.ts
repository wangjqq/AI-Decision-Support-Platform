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

/** Agent 编排相关 RTK Query API（占位） */
export const agentApi = createApi({
  reducerPath: 'agentApi',
  baseQuery: baseQueryWithRestResponse,
  tagTypes: ['Agent', 'AgentTask'],
  endpoints: (build) => ({
    getAgents: build.query<unknown, void>({
      query: () => ({ url: 'agents' }),
      providesTags: [{ type: 'Agent', id: 'LIST' }],
    }),
    getAgentTask: build.query<unknown, string>({
      query: (taskId) => ({ url: `agents/tasks/${taskId}` }),
      providesTags: (_r, _e, taskId) => [{ type: 'AgentTask', id: taskId }],
    }),
  }),
})

export const { useGetAgentsQuery, useGetAgentTaskQuery } = agentApi
