import { createApi } from '@reduxjs/toolkit/query/react'
import { createRestResponseBaseQuery, type RestResponse } from './http'

/** 公司分页查询参数 */
export interface CompanyPageParams {
  page?: number
  size?: number
  keyword?: string
  industryId?: number
}

/** 公司财务核心指标（详情中嵌套在 financial 字段下） */
export interface CompanyFinancial {
  revenue?: number
  profit?: number
  period?: string
}

/** 公司 VO（与后端 CompanyVO 对齐） */
export interface CompanyVO {
  id: number
  name: string
  /** 股票代码（如 002837） */
  code?: string
  uscc: string
  industryId?: number
  industryName?: string
  /** 细分行业（如 "液冷设备"） */
  industry?: string
  mainBusiness: string
  /** 业务板块列表 */
  business?: string[]
  address?: string
  establishedAt?: string
  description?: string
  financial?: CompanyFinancial
  createdAt?: string
  updatedAt?: string
}

/** 公司分页响应 */
export interface CompanyPageResponse {
  list: CompanyVO[]
  total: number
  page: number
  size: number
  pages: number
}

/** 创建公司请求 */
export interface CompanyCreateRequest {
  name: string
  code?: string
  uscc: string
  industryId: number
  industry?: string
  mainBusiness: string
  business?: string[]
  address?: string
  establishedAt?: string
  description?: string
  financial?: CompanyFinancial
}

/** 更新公司请求 */
export interface CompanyUpdateRequest {
  name: string
  code?: string
  uscc: string
  industryId: number
  industry?: string
  mainBusiness: string
  business?: string[]
  address?: string
  establishedAt?: string
  description?: string
  financial?: CompanyFinancial
}

/** 公司分析请求 */
export interface CompanyAnalysisRequest {
  query?: string
  topK?: number
  context?: Record<string, unknown>
}

/** 单维度结果（公司概览 / 主营业务 / 核心优势 / 潜在风险 / AI 结论） */
export interface CompanyDimension {
  title: string
  icon?: string
  color?: string
  summary?: string
  keyPoints?: string[]
  metrics?: Record<string, string>
}

/** 公司分析引用参考 */
export interface CompanyAnalysisReference {
  title: string
  url?: string
  snippet?: string
}

/** 公司分析结果（5 维度） */
export interface CompanyAnalysisResult {
  analysisId: string
  companyId: number
  companyName: string
  tookMs: number
  createdAt: string
  overview?: CompanyDimension
  mainBusiness?: CompanyDimension
  advantages?: CompanyDimension
  risks?: CompanyDimension
  aiConclusion?: CompanyDimension
  references?: CompanyAnalysisReference[]
}

/** 公司分析历史项（简版） */
export interface CompanyAnalysisHistoryItem {
  analysisId: string
  companyId: number
  tookMs: number
  createdAt: string
  snippet?: string
}

/** 公司分析历史分页响应 */
export interface CompanyAnalysisHistoryResponse {
  list: CompanyAnalysisHistoryItem[]
  total: number
  page: number
  size: number
  pages: number
}

/** 公司相关 RTK Query API */
export const companyApi = createApi({
  reducerPath: 'companyApi',
  baseQuery: createRestResponseBaseQuery('/api/v1/'),
  tagTypes: ['Company', 'CompanyAnalysis'] as const,
  endpoints: (build) => ({
    // 分页查询
    getCompanies: build.query<CompanyPageResponse, CompanyPageParams>({
      query: ({ page = 1, size = 20, keyword, industryId }) => ({
        url: 'companies',
        params: { page, size, keyword, industryId },
      }),
      transformResponse: (raw: RestResponse<CompanyPageResponse> | CompanyPageResponse) => {
        // 容错：若 baseQuery 已解包则直接返回，否则从 RestResponse.data 取
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<CompanyPageResponse>).data ?? { list: [], total: 0, page: 1, size: 20, pages: 0 }
        }
        return raw as CompanyPageResponse
      },
      providesTags: (result) =>
        result
          ? [
              ...result.list.map((c) => ({ type: 'Company' as const, id: c.id })),
              { type: 'Company' as const, id: 'LIST' },
            ]
          : [{ type: 'Company' as const, id: 'LIST' }],
    }),

    // 详情
    getCompanyById: build.query<CompanyVO, number>({
      query: (id) => ({ url: `companies/${id}` }),
      transformResponse: (raw: RestResponse<CompanyVO> | CompanyVO) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<CompanyVO>).data as CompanyVO
        }
        return raw as CompanyVO
      },
      providesTags: (_result, _error, id) => [{ type: 'Company' as const, id }],
    }),

    // 新增
    createCompany: build.mutation<CompanyVO, CompanyCreateRequest>({
      query: (body) => ({ url: 'companies', method: 'POST', body }),
      transformResponse: (raw: RestResponse<CompanyVO> | CompanyVO) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<CompanyVO>).data as CompanyVO
        }
        return raw as CompanyVO
      },
      invalidatesTags: [{ type: 'Company', id: 'LIST' }],
    }),

    // 更新
    updateCompany: build.mutation<CompanyVO, { id: number; body: CompanyUpdateRequest }>({
      query: ({ id, body }) => ({ url: `companies/${id}`, method: 'PUT', body }),
      transformResponse: (raw: RestResponse<CompanyVO> | CompanyVO) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<CompanyVO>).data as CompanyVO
        }
        return raw as CompanyVO
      },
      invalidatesTags: (_result, _error, { id }) => [
        { type: 'Company', id },
        { type: 'Company', id: 'LIST' },
      ],
    }),

    // 删除
    deleteCompany: build.mutation<void, number>({
      query: (id) => ({ url: `companies/${id}`, method: 'DELETE' }),
      invalidatesTags: (_result, _error, id) => [
        { type: 'Company', id },
        { type: 'Company', id: 'LIST' },
      ],
    }),

    // 触发分析
    analyzeCompany: build.mutation<CompanyAnalysisResult, { id: number; body: CompanyAnalysisRequest }>({
      query: ({ id, body }) => ({
        url: `companies/${id}/analyses`,
        method: 'POST',
        body: body ?? {},
      }),
      transformResponse: (raw: RestResponse<CompanyAnalysisResult> | CompanyAnalysisResult) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<CompanyAnalysisResult>).data as CompanyAnalysisResult
        }
        return raw as CompanyAnalysisResult
      },
      invalidatesTags: (_result, _error, { id }) => [{ type: 'CompanyAnalysis', id: `LIST-${id}` }],
    }),

    // 某公司分析历史
    getCompanyAnalysisHistory: build.query<
      CompanyAnalysisHistoryResponse,
      { id: number; page?: number; size?: number }
    >({
      query: ({ id, page = 1, size = 20 }) => ({
        url: `companies/${id}/analyses`,
        params: { page, size },
      }),
      transformResponse: (raw: RestResponse<CompanyAnalysisHistoryResponse> | CompanyAnalysisHistoryResponse) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (
            (raw as RestResponse<CompanyAnalysisHistoryResponse>).data ?? {
              list: [],
              total: 0,
              page: 1,
              size: 20,
              pages: 0,
            }
          )
        }
        return raw as CompanyAnalysisHistoryResponse
      },
      providesTags: (_result, _error, { id }) => [{ type: 'CompanyAnalysis', id: `LIST-${id}` }],
    }),

    // 单次分析详情
    getCompanyAnalysisById: build.query<CompanyAnalysisResult, { id: number; analysisId: string }>({
      query: ({ id, analysisId }) => ({ url: `companies/${id}/analyses/${analysisId}` }),
      transformResponse: (raw: RestResponse<CompanyAnalysisResult> | CompanyAnalysisResult) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<CompanyAnalysisResult>).data as CompanyAnalysisResult
        }
        return raw as CompanyAnalysisResult
      },
      providesTags: (_result, _error, { analysisId }) => [{ type: 'CompanyAnalysis', id: analysisId }],
    }),

    // 删除单次分析
    deleteCompanyAnalysis: build.mutation<void, { id: number; analysisId: string }>({
      query: ({ id, analysisId }) => ({
        url: `companies/${id}/analyses/${analysisId}`,
        method: 'DELETE',
      }),
      invalidatesTags: (_result, _error, { id, analysisId }) => [
        { type: 'CompanyAnalysis', id: `LIST-${id}` },
        { type: 'CompanyAnalysis', id: analysisId },
      ],
    }),
  }),
})

export const {
  useGetCompaniesQuery,
  useGetCompanyByIdQuery,
  useCreateCompanyMutation,
  useUpdateCompanyMutation,
  useDeleteCompanyMutation,
  useAnalyzeCompanyMutation,
  useGetCompanyAnalysisHistoryQuery,
  useGetCompanyAnalysisByIdQuery,
  useDeleteCompanyAnalysisMutation,
} = companyApi
