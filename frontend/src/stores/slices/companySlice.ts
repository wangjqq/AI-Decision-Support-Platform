import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { CompanyAnalysisResult, CompanyVO } from '../../api/companyApi'

/** Company 模块 UI/会话态（不含历史；历史走后端分页） */
export interface CompanyState {
  /** 当前在公司详情页右栏展示的 5 维度结果（来自"立即分析"或点历史） */
  currentAnalysis: CompanyAnalysisResult | null
  /** 当前结果对应的 analysisId（用于历史列表高亮） */
  currentAnalysisId: string | null
  /** 新增/编辑 Modal 开关 */
  editorOpen: boolean
  editorMode: 'create' | 'edit'
  /** 编辑器中正在编辑的实体（edit 模式） */
  editingCompany: CompanyVO | null
}

const initialState: CompanyState = {
  currentAnalysis: null,
  currentAnalysisId: null,
  editorOpen: false,
  editorMode: 'create',
  editingCompany: null,
}

const companySlice = createSlice({
  name: 'company',
  initialState,
  reducers: {
    /** 设置当前展示的 5 维度结果 */
    setCurrentAnalysis(state, action: PayloadAction<CompanyAnalysisResult | null>) {
      state.currentAnalysis = action.payload
      state.currentAnalysisId = action.payload?.analysisId ?? null
    },
    /** 打开新增 Modal */
    openCreateEditor(state) {
      state.editorOpen = true
      state.editorMode = 'create'
      state.editingCompany = null
    },
    /** 打开编辑 Modal（传入目标公司） */
    openEditEditor(state, action: PayloadAction<CompanyVO>) {
      state.editorOpen = true
      state.editorMode = 'edit'
      state.editingCompany = action.payload
    },
    /** 关闭编辑器 */
    closeEditor(state) {
      state.editorOpen = false
      state.editingCompany = null
    },
    /** 清空当前结果（删除历史后回退等场景） */
    clearCurrentAnalysis(state) {
      state.currentAnalysis = null
      state.currentAnalysisId = null
    },
  },
})

export const { setCurrentAnalysis, openCreateEditor, openEditEditor, closeEditor, clearCurrentAnalysis } =
  companySlice.actions

export default companySlice.reducer
