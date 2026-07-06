import { useDispatch, useSelector, type TypedUseSelectorHook } from 'react-redux'
import type { AppDispatch, RootState } from '../stores'

/** 类型化的 dispatch hook：业务侧统一使用 */
export const useAppDispatch: () => AppDispatch = useDispatch

/** 类型化的 selector hook：业务侧统一使用，自动推导 RootState */
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
