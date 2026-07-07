import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'

import appReducer from './slices/appSlice'
import searchReducer from './slices/searchSlice'
import companyReducer from './slices/companySlice'
import industryReducer from './slices/industrySlice'
import { companyApi } from '../api/companyApi'
import { industryApi } from '../api/industryApi'
import { reportApi } from '../api/reportApi'
import { agentApi } from '../api/agentApi'
import { analysisApi } from '../api/analysisApi'

/**
 * 全局 Redux store
 * - 合并 appSlice / searchSlice / companySlice / industrySlice（UI 状态）
 * - 注册各业务域的 RTK Query reducer / middleware
 */
export const store = configureStore({
  reducer: {
    app: appReducer,
    search: searchReducer,
    company: companyReducer,
    industry: industryReducer,
    [companyApi.reducerPath]: companyApi.reducer,
    [industryApi.reducerPath]: industryApi.reducer,
    [reportApi.reducerPath]: reportApi.reducer,
    [agentApi.reducerPath]: agentApi.reducer,
    [analysisApi.reducerPath]: analysisApi.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        // RTK Query 默认开启的 serializableCheck 在某些情况下会发出警告，按需忽略
        ignoredActions: ['api/executeQuery/pending', 'api/executeQuery/fulfilled'],
      },
    })
      .concat(companyApi.middleware)
      .concat(industryApi.middleware)
      .concat(reportApi.middleware)
      .concat(agentApi.middleware)
      .concat(analysisApi.middleware),
})

// 启用 refetchOnFocus / refetchOnReconnect 等行为
setupListeners(store.dispatch)

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
