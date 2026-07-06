import { createApi } from '@reduxjs/toolkit/query/react'
import { createRestResponseBaseQuery } from './http'

/** Agent 编排相关 RTK Query API（占位） */
export const agentApi = createApi({
  reducerPath: 'agentApi',
  baseQuery: createRestResponseBaseQuery('/api/v1/'),
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
