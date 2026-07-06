import { List, Tag, Typography, Space, Empty } from 'antd'
import { ClockCircleOutlined, ThunderboltOutlined } from '@ant-design/icons'
import type { HistoryItem, AnalysisType } from '../../../api/analysisApi'

const { Text } = Typography

/** 分析类型 → Tag 颜色映射 */
const TYPE_COLOR: Record<AnalysisType, string> = {
  COMPANY: 'blue',
  INDUSTRY: 'green',
  REPORT: 'purple',
}

/** 分析类型 → 中文标签 */
const TYPE_LABEL: Record<AnalysisType, string> = {
  COMPANY: '公司',
  INDUSTRY: '行业',
  REPORT: '报告',
}

export interface HistoryListProps {
  items: HistoryItem[]
  /** 点击某条历史 */
  onPick: (item: HistoryItem) => void
}

/**
 * 历史分析列表
 * - 空态：Empty 提示
 * - 每行：类型 Tag + query 文本 + 耗时
 * - 整行可点击
 */
const HistoryList = ({ items, onPick }: HistoryListProps) => {
  if (!items.length) {
    return <Empty description="暂无历史分析" image={Empty.PRESENTED_IMAGE_SIMPLE} />
  }

  return (
    <List<HistoryItem>
      size="small"
      dataSource={items}
      renderItem={(item) => {
        const color = TYPE_COLOR[item.analysisType] ?? 'default'
        const label = TYPE_LABEL[item.analysisType] ?? item.analysisType
        const seconds = typeof item.tookMs === 'number' ? (item.tookMs / 1000).toFixed(2) : null
        return (
          <List.Item
            onClick={() => onPick(item)}
            style={{
              cursor: 'pointer',
              padding: '10px 12px',
              borderRadius: 10,
              marginBottom: 6,
              background: 'rgba(99, 102, 241, 0.04)',
              border: '1px solid rgba(99, 102, 241, 0.08)',
              transition: 'all 0.2s',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = 'rgba(99, 102, 241, 0.10)'
              e.currentTarget.style.borderColor = 'rgba(99, 102, 241, 0.20)'
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'rgba(99, 102, 241, 0.04)'
              e.currentTarget.style.borderColor = 'rgba(99, 102, 241, 0.08)'
            }}>
            <Space size={8} wrap style={{ width: '100%' }} direction="vertical">
              <Space size={6} wrap>
                <Tag color={color} style={{ marginRight: 0 }}>
                  {label}
                </Tag>
                <Text strong style={{ color: '#0f172a' }}>
                  {item.query}
                </Text>
              </Space>
              <Space size={12} wrap style={{ fontSize: 12 }}>
                {seconds !== null && (
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    <ClockCircleOutlined style={{ marginRight: 4 }} />
                    耗时 {seconds}s
                  </Text>
                )}
                <Text type="secondary" style={{ fontSize: 12 }}>
                  <ThunderboltOutlined style={{ marginRight: 4 }} />
                  {item.queryId}
                </Text>
              </Space>
            </Space>
          </List.Item>
        )
      }}
    />
  )
}

export default HistoryList
