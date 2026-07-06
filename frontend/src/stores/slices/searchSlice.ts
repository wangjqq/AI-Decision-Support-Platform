import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type {
  AnalysisResultDTO,
  HistoryItem,
  AnalysisType,
} from '../../api/analysisApi'

/** localStorage 持久化 key */
export const SEARCH_HISTORY_LS_KEY = 'aidsp_search_history'
/** 最多保留的历史条数（LRU 淘汰阈值） */
export const SEARCH_HISTORY_MAX = 10

/** Search 模块的 UI/会话状态 */
export interface SearchState {
  /** 当前输入框内容 */
  inputValue: string
  /** 历史分析记录（最近 N 条，最近的在前） */
  history: HistoryItem[]
  /** 当前展示的分析结果 */
  currentResult: AnalysisResultDTO | null
}

/** 从 localStorage 安全读取历史 */
const loadHistory = (): HistoryItem[] => {
  if (typeof window === 'undefined') return []
  try {
    const raw = window.localStorage.getItem(SEARCH_HISTORY_LS_KEY)
    if (!raw) return []
    const parsed: unknown = JSON.parse(raw)
    if (!Array.isArray(parsed)) return []
    // 简单过滤掉非法的项
    return parsed
      .filter(
        (it): it is HistoryItem =>
          typeof it === 'object' &&
          it !== null &&
          typeof (it as HistoryItem).queryId === 'string' &&
          typeof (it as HistoryItem).query === 'string' &&
          typeof (it as HistoryItem).analysisType === 'string',
      )
      .slice(0, SEARCH_HISTORY_MAX)
  } catch {
    return []
  }
}

/** 将历史持久化到 localStorage（失败静默） */
const saveHistory = (history: HistoryItem[]): void => {
  if (typeof window === 'undefined') return
  try {
    window.localStorage.setItem(SEARCH_HISTORY_LS_KEY, JSON.stringify(history))
  } catch {
    /* ignore quota / private mode */
  }
}

const initialState: SearchState = {
  inputValue: '',
  history: loadHistory(),
  currentResult: null,
}

const searchSlice = createSlice({
  name: 'search',
  initialState,
  reducers: {
    /** 更新输入框受控值 */
    setInput: (state, action: PayloadAction<string>) => {
      state.inputValue = action.payload
    },
    /**
     * 推入一条历史（LRU + 去重）：
     * - 已有相同 queryId 的会先移除再插入到头部
     * - 超出 MAX 的尾部淘汰
     */
    pushHistory: (state, action: PayloadAction<HistoryItem>) => {
      const incoming = action.payload
      const filtered = state.history.filter((h) => h.queryId !== incoming.queryId)
      state.history = [incoming, ...filtered].slice(0, SEARCH_HISTORY_MAX)
      saveHistory(state.history)
    },
    /** 设置当前展示的分析结果（用于回显历史记录） */
    setCurrentResult: (state, action: PayloadAction<AnalysisResultDTO | null>) => {
      state.currentResult = action.payload
    },
    /** 清空历史（UI + localStorage） */
    clearHistory: (state) => {
      state.history = []
      saveHistory([])
    },
    /**
     * 同步补全当前结果的历史条目（用于从历史列表点回时，
     * 列表里只有 summary，详情里 AnalysisResultDTO 包含完整 result）
     */
    syncCurrentToHistory: (state) => {
      if (!state.currentResult) return
      const id = state.currentResult.queryId
      const idx = state.history.findIndex((h) => h.queryId === id)
      if (idx === -1) {
        // 不在历史里：插入一条（拿不到 query 原文时使用 queryId 兜底）
        const item: HistoryItem = {
          queryId: id,
          query: id,
          analysisType: state.currentResult.analysisType as AnalysisType,
          tookMs: state.currentResult.tookMs,
          createdAt: state.currentResult.createdAt,
        }
        state.history = [item, ...state.history].slice(0, SEARCH_HISTORY_MAX)
      }
      saveHistory(state.history)
    },
  },
})

export const {
  setInput,
  pushHistory,
  setCurrentResult,
  clearHistory,
  syncCurrentToHistory,
} = searchSlice.actions

export default searchSlice.reducer
