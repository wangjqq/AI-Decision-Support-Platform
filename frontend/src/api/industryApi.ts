import { createApi } from '@reduxjs/toolkit/query/react'
import { createRestResponseBaseQuery } from './http'

/** 行业相关 RTK Query API */
export const industryApi = createApi({
  reducerPath: 'industryApi',
  baseQuery: createRestResponseBaseQuery('/api/v1/'),
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
