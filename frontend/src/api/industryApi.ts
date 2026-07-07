import { createApi } from '@reduxjs/toolkit/query/react'
import { createRestResponseBaseQuery, type RestResponse } from './http'

/** 行业分页查询参数 */
export interface IndustryPageParams {
  page?: number
  size?: number
  keyword?: string
  level?: number
  parentId?: number
}

/** 行业 VO（与后端 IndustryVO 对齐） */
export interface IndustryVO {
  id: number
  code?: string
  name: string
  level?: number
  parentId?: number
  parentName?: string
  description?: string
  tags?: string
  status?: number
  createdAt?: string
  updatedAt?: string
}

/** 行业分页响应 */
export interface IndustryPageResponse {
  list: IndustryVO[]
  total: number
  page: number
  size: number
  pages: number
}

/** 创建行业请求 */
export interface IndustryCreateRequest {
  code: string
  name: string
  level: number
  parentId?: number
  description?: string
  tags?: string
}

/** 更新行业请求 */
export interface IndustryUpdateRequest {
  name: string
  level: number
  parentId?: number
  description?: string
  tags?: string
}

/** 行业分析请求 */
export interface IndustryAnalysisRequest {
  query?: string
  topK?: number
  context?: Record<string, unknown>
}

/** 单维度结果（行业概况 / 市场空间 / 产业链结构 / 龙头企业 / 未来趋势 / 风险分析） */
export interface IndustryDimension {
  title: string
  icon?: string
  color?: string
  summary?: string
  keyPoints?: string[]
  metrics?: Record<string, string>
}

/** 产业链节点 */
export interface IndustryChainNode {
  name: string
  type?: 'UPSTREAM' | 'MIDSTREAM' | 'DOWNSTREAM' | string
  description?: string
  representatives?: string
}

/** 龙头企业 */
export interface IndustryLeadingCompany {
  name: string
  stockCode?: string
  marketShare?: number
  tag?: string
  description?: string
}

/** 行业分析引用 */
export interface IndustryAnalysisReference {
  title: string
  url?: string
  snippet?: string
}

/** 行业分析结果（6 维度） */
export interface IndustryAnalysisResult {
  analysisId: string
  industryId: number
  industryName: string
  tookMs: number
  createdAt: string
  overview?: IndustryDimension
  marketSize?: IndustryDimension
  chain?: IndustryDimension
  leading?: IndustryDimension
  trends?: IndustryDimension
  risks?: IndustryDimension
  chainNodes?: IndustryChainNode[]
  leadingCompanies?: IndustryLeadingCompany[]
  references?: IndustryAnalysisReference[]
}

/** 行业分析历史项 */
export interface IndustryAnalysisHistoryItem {
  analysisId: string
  industryId: number
  tookMs: number
  createdAt: string
  snippet?: string
}

/** 行业分析历史分页响应 */
export interface IndustryAnalysisHistoryResponse {
  list: IndustryAnalysisHistoryItem[]
  total: number
  page: number
  size: number
  pages: number
}

/** 行业相关 RTK Query API */
export const industryApi = createApi({
  reducerPath: 'industryApi',
  baseQuery: createRestResponseBaseQuery('/api/v1/'),
  tagTypes: ['Industry', 'IndustryAnalysis'] as const,
  endpoints: (build) => ({
    // 分页查询
    getIndustries: build.query<IndustryPageResponse, IndustryPageParams>({
      query: ({ page = 1, size = 20, keyword, level, parentId }) => ({
        url: 'industries',
        params: { page, size, keyword, level, parentId },
      }),
      transformResponse: (raw: RestResponse<IndustryPageResponse> | IndustryPageResponse) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (
            (raw as RestResponse<IndustryPageResponse>).data ?? {
              list: [],
              total: 0,
              page: 1,
              size: 20,
              pages: 0,
            }
          )
        }
        return raw as IndustryPageResponse
      },
      providesTags: (result) =>
        result
          ? [
              ...result.list.map((i) => ({ type: 'Industry' as const, id: i.id })),
              { type: 'Industry' as const, id: 'LIST' },
            ]
          : [{ type: 'Industry' as const, id: 'LIST' }],
    }),

    // 详情
    getIndustryById: build.query<IndustryVO, number>({
      query: (id) => ({ url: `industries/${id}` }),
      transformResponse: (raw: RestResponse<IndustryVO> | IndustryVO) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<IndustryVO>).data as IndustryVO
        }
        return raw as IndustryVO
      },
      providesTags: (_result, _error, id) => [{ type: 'Industry' as const, id }],
    }),

    // 新增
    createIndustry: build.mutation<IndustryVO, IndustryCreateRequest>({
      query: (body) => ({ url: 'industries', method: 'POST', body }),
      transformResponse: (raw: RestResponse<IndustryVO> | IndustryVO) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<IndustryVO>).data as IndustryVO
        }
        return raw as IndustryVO
      },
      invalidatesTags: [{ type: 'Industry', id: 'LIST' }],
    }),

    // 更新
    updateIndustry: build.mutation<IndustryVO, { id: number; body: IndustryUpdateRequest }>({
      query: ({ id, body }) => ({ url: `industries/${id}`, method: 'PUT', body }),
      transformResponse: (raw: RestResponse<IndustryVO> | IndustryVO) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<IndustryVO>).data as IndustryVO
        }
        return raw as IndustryVO
      },
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Industry', id },
        { type: 'Industry', id: 'LIST' },
      ],
    }),

    // 删除
    deleteIndustry: build.mutation<void, number>({
      query: (id) => ({ url: `industries/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'Industry', id },
        { type: 'Industry', id: 'LIST' },
      ],
    }),

    // 触发分析
    analyzeIndustry: build.mutation<IndustryAnalysisResult, { id: number; body: IndustryAnalysisRequest }>({
      query: ({ id, body }) => ({
        url: `industries/${id}/analyses`,
        method: 'POST',
        body: body ?? {},
      }),
      transformResponse: (raw: RestResponse<IndustryAnalysisResult> | IndustryAnalysisResult) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<IndustryAnalysisResult>).data as IndustryAnalysisResult
        }
        return raw as IndustryAnalysisResult
      },
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'IndustryAnalysis', id: `LIST-${id}` },
      ],
    }),

    // 某行业分析历史
    getIndustryAnalysisHistory: build.query<
      IndustryAnalysisHistoryResponse,
      { id: number; page?: number; size?: number }
    >({
      query: ({ id, page = 1, size = 20 }) => ({
        url: `industries/${id}/analyses`,
        params: { page, size },
      }),
      transformResponse: (raw: RestResponse<IndustryAnalysisHistoryResponse> | IndustryAnalysisHistoryResponse) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (
            (raw as RestResponse<IndustryAnalysisHistoryResponse>).data ?? {
              list: [],
              total: 0,
              page: 1,
              size: 20,
              pages: 0,
            }
          )
        }
        return raw as IndustryAnalysisHistoryResponse
      },
      providesTags: (_result, _error, { id }) => [
        { type: 'IndustryAnalysis', id: `LIST-${id}` },
      ],
    }),

    // 单次分析详情
    getIndustryAnalysisById: build.query<IndustryAnalysisResult, { id: number; analysisId: string }>({
      query: ({ id, analysisId }) => ({ url: `industries/${id}/analyses/${analysisId}` }),
      transformResponse: (raw: RestResponse<IndustryAnalysisResult> | IndustryAnalysisResult) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<IndustryAnalysisResult>).data as IndustryAnalysisResult
        }
        return raw as IndustryAnalysisResult
      },
      providesTags: (_result, _error, { analysisId }) => [
        { type: 'IndustryAnalysis', id: analysisId },
      ],
    }),

    // 删除单次分析
    deleteIndustryAnalysis: build.mutation<void, { id: number; analysisId: string }>({
      query: ({ id, analysisId }) => ({
        url: `industries/${id}/analyses/${analysisId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, { id, analysisId }) => [
        { type: 'IndustryAnalysis', id: `LIST-${id}` },
        { type: 'IndustryAnalysis', id: analysisId },
      ],
    }),
  }),
})

export const {
  useGetIndustriesQuery,
  useGetIndustryByIdQuery,
  useCreateIndustryMutation,
  useUpdateIndustryMutation,
  useDeleteIndustryMutation,
  useAnalyzeIndustryMutation,
  useGetIndustryAnalysisHistoryQuery,
  useGetIndustryAnalysisByIdQuery,
  useDeleteIndustryAnalysisMutation,
} = industryApi
