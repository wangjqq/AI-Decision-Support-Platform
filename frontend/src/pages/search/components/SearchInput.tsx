import { Input } from 'antd'

/** 智能分析页输入框 Props */
export interface SearchInputProps {
  /** 受控值 */
  value: string
  /** 是否处于 loading（用于禁用/loading 状态） */
  loading: boolean
  /** 输入内容变化 */
  onChange: (value: string) => void
  /** 提交（点击按钮 / 回车） */
  onSubmit: (value: string) => void
}

/**
 * 顶部搜索输入：受控 Input.Search。
 * - 大尺寸、玻璃化风格由全局 antd theme + index.css 接管
 * - 提交时机：点击 "开始分析" 按钮 或 在输入框内按回车
 */
const SearchInput = ({ value, loading, onChange, onSubmit }: SearchInputProps) => {
  return (
    <Input.Search
      size="large"
      enterButton="开始分析"
      loading={loading}
      placeholder="试试：分析宁德时代 / 光伏行业前景 / 2025 AI 芯片报告"
      allowClear
      value={value}
      onChange={(e) => onChange(e.target.value)}
      onSearch={(v) => onSubmit(v)}
    />
  )
}

export default SearchInput
