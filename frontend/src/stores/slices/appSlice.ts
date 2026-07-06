import { createSlice, type PayloadAction } from '@reduxjs/toolkit'

/** 全局 UI 状态：侧边栏折叠、当前选中菜单等 */
export interface AppState {
  collapsed: boolean
  activeMenuKey: string
}

const initialState: AppState = {
  collapsed: false,
  activeMenuKey: '/dashboard',
}

const appSlice = createSlice({
  name: 'app',
  initialState,
  reducers: {
    toggleCollapsed: (state) => {
      state.collapsed = !state.collapsed
    },
    setCollapsed: (state, action: PayloadAction<boolean>) => {
      state.collapsed = action.payload
    },
    setActiveMenuKey: (state, action: PayloadAction<string>) => {
      state.activeMenuKey = action.payload
    },
  },
})

export const { toggleCollapsed, setCollapsed, setActiveMenuKey } = appSlice.actions
export default appSlice.reducer
