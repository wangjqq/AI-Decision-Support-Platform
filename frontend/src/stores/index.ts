import { configureStore } from '@reduxjs/toolkit'
import { setupListeners } from '@reduxjs/toolkit/query'

import appReducer from './slices/appSlice'
import { companyApi } from '../api/companyApi'
import { industryApi } from '../api/industryApi'
import { reportApi } from '../api/reportApi'
import { agentApi } from '../api/agentApi'

/**
 * 全局 Redux store
 * - 合并 appSlice（UI 状态）
 * - 注册各业务域的 RTK Query reducer / middleware
 */
export const store = configureStore({
  reducer: {
    app: appReducer,
    [companyApi.reducerPath]: companyApi.reducer,
    [industryApi.reducerPath]: industryApi.reducer,
    [reportApi.reducerPath]: reportApi.reducer,
    [agentApi.reducerPath]: agentApi.reducer,
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
      .concat(agentApi.middleware),
})

// 启用 refetchOnFocus / refetchOnReconnect 等行为
setupListeners(store.dispatch)

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
