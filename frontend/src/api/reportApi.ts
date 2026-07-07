import { createApi } from '@reduxjs/toolkit/query/react'
import { createRestResponseBaseQuery, type RestResponse } from './http'
import type { CompanyAnalysisResult } from './companyApi'
import type { IndustryAnalysisResult } from './industryApi'

// ===================== 类型定义 =====================

/** 报告目录条目 */
export interface ReportTocItem {
  anchor: string
  title: string
  level?: number
}

/** 报告章节 */
export interface ReportSection {
  key: string
  title: string
  anchor: string
  markdown: string
}

/** 报告参考引用 */
export interface ReportReference {
  title: string
  url?: string
  snippet?: string
  sourceType?: 'INDUSTRY' | 'COMPANY' | 'POLICY' | 'NEWS' | string
}

/** 报告详情 VO（与后端 ReportVO 对齐） */
export interface ReportDetail {
  reportId: string
  title: string
  type: 'COMPANY' | 'INDUSTRY' | 'COMPREHENSIVE' | string
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | string
  companyId: number
  companyName: string
  industryId?: number
  industryName?: string
  companyAnalysisId: string
  industryAnalysisId?: string
  summary?: string
  summaryMarkdown?: string
  toc: ReportTocItem[]
  sections: ReportSection[]
  markdown: string
  references: ReportReference[]
  tookMs: number
  createdAt: string
  updatedAt: string
}

/** 报告历史列表项（与后端 ReportHistoryItemVO 对齐） */
export interface ReportHistoryItem {
  reportId: string
  title: string
  companyId: number
  companyName: string
  industryId?: number
  industryName?: string
  status: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | string
  tookMs: number
  createdAt: string
  summary?: string
}

/** 报告分页响应 */
export interface ReportPageResponse {
  list: ReportHistoryItem[]
  total: number
  page: number
  size: number
  pages: number
}

/** 报告生成请求（与后端 ReportGenerateRequest 对齐） */
export interface ReportGenerateRequest {
  companyId: number
  companyName?: string
  companyAnalysisId: string
  companyAnalysis: CompanyAnalysisResult
  industryId?: number
  industryName?: string
  industryAnalysisId?: string
  industryAnalysis?: IndustryAnalysisResult
  title?: string
  query?: string
}

/** 报告分页查询参数 */
export interface ReportPageParams {
  page?: number
  size?: number
  keyword?: string
  companyId?: number
}

// ===================== RTK Query =====================

/** 报告相关 RTK Query API */
export const reportApi = createApi({
  reducerPath: 'reportApi',
  baseQuery: createRestResponseBaseQuery('/api/v1/'),
  tagTypes: ['Report', 'ReportList'] as const,
  endpoints: (build) => ({
    // 分页查询报告列表
    getReports: build.query<ReportPageResponse, ReportPageParams>({
      query: ({ page = 1, size = 20, keyword, companyId }: ReportPageParams) => ({
        url: 'reports',
        params: { page, size, keyword, companyId },
      }),
      transformResponse: (raw: RestResponse<ReportPageResponse> | ReportPageResponse) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (
            (raw as RestResponse<ReportPageResponse>).data ?? {
              list: [],
              total: 0,
              page: 1,
              size: 20,
              pages: 0,
            }
          )
        }
        return raw as ReportPageResponse
      },
      providesTags: (result) =>
        result
          ? [
              ...(result.list.map((r) => ({ type: 'Report' as const, id: r.reportId }))),
              { type: 'ReportList' as const, id: 'LIST' },
            ]
          : [{ type: 'ReportList' as const, id: 'LIST' }],
    }),

    // 报告详情
    getReportById: build.query<ReportDetail, string>({
      query: (id) => ({ url: `reports/${id}` }),
      transformResponse: (raw: RestResponse<ReportDetail> | ReportDetail) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<ReportDetail>).data as ReportDetail
        }
        return raw as ReportDetail
      },
      providesTags: (_r, _e, id) => [{ type: 'Report' as const, id }],
    }),

    // 生成报告（同步调用 Agent，1.5-2.5s 返回完整 VO）
    generateReport: build.mutation<ReportDetail, ReportGenerateRequest>({
      query: (body) => ({
        url: 'reports/generate',
        method: 'POST',
        body,
      }),
      transformResponse: (raw: RestResponse<ReportDetail> | ReportDetail) => {
        if (raw && typeof raw === 'object' && 'code' in raw) {
          return (raw as RestResponse<ReportDetail>).data as ReportDetail
        }
        return raw as ReportDetail
      },
      invalidatesTags: [{ type: 'ReportList' as const, id: 'LIST' }],
    }),

    // 删除报告
    deleteReport: build.mutation<void, string>({
      query: (id) => ({ url: `reports/${id}`, method: 'DELETE' }),
      invalidatesTags: (_r, _e, id) => [
        { type: 'Report' as const, id },
        { type: 'ReportList' as const, id: 'LIST' },
      ],
    }),
  }),
})

export const {
  useGetReportsQuery,
  useGetReportByIdQuery,
  useGenerateReportMutation,
  useDeleteReportMutation,
} = reportApi
