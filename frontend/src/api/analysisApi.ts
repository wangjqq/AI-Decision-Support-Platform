import { createApi } from '@reduxjs/toolkit/query/react'
import { createRestResponseBaseQuery } from './http'

/** 智能分析路由类型：系统会根据 query 内容自动判断走哪种 Agent */
export type AnalysisType = 'COMPANY' | 'INDUSTRY' | 'REPORT'

/** 提交一次分析请求的入参 */
export interface AnalysisQueryRequest {
  /** 用户的自然语言查询，例如 "分析宁德时代" */
  query: string
  /** 召回 Top K 条参考（可选；不传则使用后端默认） */
  topK?: number
  /** 额外的上下文（行业/公司/报告 ID 等） */
  context?: Record<string, unknown>
}

/** 单条引用参考（来自知识库/报告/公司） */
export interface AnalysisReference {
  id: string
  title: string
  type: AnalysisType
  snippet?: string
  url?: string
}

/** 分析结果正文 */
export interface AnalysisResultPayload {
  /** 总结性段落（直接 copyable 展示） */
  summary: string
  /** 关键要点列表 */
  keyPoints: string[]
  /** 关键指标 kv：例如 { "毛利率": "23.5%", "PE": "18.2" } */
  metrics: Record<string, string | number>
  /** 引用参考 */
  references: AnalysisReference[]
}

/** 单次智能分析的标准返回 */
export interface AnalysisResultDTO {
  queryId: string
  analysisType: AnalysisType
  result: AnalysisResultPayload
  /** 编排耗时（毫秒），用于 UI 展示 */
  tookMs?: number
  createdAt?: string
}

/** 历史分析记录（列表项） */
export interface HistoryItem {
  queryId: string
  query: string
  analysisType: AnalysisType
  tookMs?: number
  createdAt?: string
}

/** 历史分析分页响应 */
export interface HistoryListResponse {
  list: HistoryItem[]
  total: number
}

/** 智能分析 RTK Query API */
export const analysisApi = createApi({
  reducerPath: 'analysisApi',
  baseQuery: createRestResponseBaseQuery('/api/v1/'),
  tagTypes: ['Analysis'],
  endpoints: (build) => ({
    /** 提交一次查询，返回 AnalysisResultDTO */
    postQuery: build.mutation<AnalysisResultDTO, AnalysisQueryRequest>({
      query: (body) => ({
        url: 'analysis/query',
        method: 'POST',
        body,
      }),
      invalidatesTags: [{ type: 'Analysis', id: 'LIST' }],
    }),
    /** 获取历史分析（分页） */
    getHistory: build.query<HistoryListResponse, { page: number; size: number }>({
      query: ({ page, size }) => ({
        url: 'analysis/history',
        params: { page, size },
      }),
      providesTags: [{ type: 'Analysis', id: 'LIST' }],
    }),
    /** 按 queryId 拉取完整结果 */
    getById: build.query<AnalysisResultDTO, string>({
      query: (queryId) => ({ url: `analysis/${encodeURIComponent(queryId)}` }),
      providesTags: (_r, _e, id) => [{ type: 'Analysis', id }],
    }),
  }),
})

export const {
  usePostQueryMutation,
  useGetHistoryQuery,
  useGetByIdQuery,
} = analysisApi
