import { createApi } from '@reduxjs/toolkit/query/react'
import { createRestResponseBaseQuery } from './http'

/** 报告相关 RTK Query API */
export const reportApi = createApi({
  reducerPath: 'reportApi',
  baseQuery: createRestResponseBaseQuery('/api/v1/'),
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
