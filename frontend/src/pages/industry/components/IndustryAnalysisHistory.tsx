import { Card, List, Tag, Button, Space, Typography, Popconfirm, Empty, Skeleton } from 'antd'
import {
  ClockCircleOutlined,
  DeleteOutlined,
  HistoryOutlined,
  FileTextOutlined,
} from '@ant-design/icons'
import type { IndustryAnalysisHistoryItem } from '../../../api/industryApi'

const { Text } = Typography

export interface IndustryAnalysisHistoryProps {
  items: IndustryAnalysisHistoryItem[]
  loading?: boolean
  currentAnalysisId?: string | null
  onPick?: (analysisId: string) => void
  onDelete?: (analysisId: string) => void
  deletingId?: string | null
}

/**
 * 行业分析历史列表
 * - 点击条目 → 回显 6 维度
 * - 删除按钮 → 二次确认后删除
 */
const IndustryAnalysisHistory = ({
  items,
  loading,
  currentAnalysisId,
  onPick,
  onDelete,
  deletingId,
}: IndustryAnalysisHistoryProps) => {
  return (
    <Card
      className="glass-card"
      title={
        <Space>
          <HistoryOutlined style={{ color: '#6366f1' }} />
          <span className="gradient-text">分析历史</span>
        </Space>
      }
      bodyStyle={{ padding: 12 }}>
      {loading ? (
        <Skeleton active />
      ) : items.length === 0 ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<Text type="secondary">暂无分析记录</Text>}
        />
      ) : (
        <List<IndustryAnalysisHistoryItem>
          size="small"
          dataSource={items}
          renderItem={(item) => {
            const active = currentAnalysisId === item.analysisId
            return (
              <List.Item
                style={{
                  padding: '10px 12px',
                  borderRadius: 10,
                  marginBottom: 6,
                  cursor: onPick ? 'pointer' : 'default',
                  background: active ? 'rgba(99,102,241,0.10)' : 'rgba(255,255,255,0.5)',
                  border: active ? '1px solid rgba(99,102,241,0.3)' : '1px solid transparent',
                  transition: 'all 0.2s',
                }}
                onClick={() => onPick?.(item.analysisId)}
                actions={
                  onDelete
                    ? [
                        <Popconfirm
                          key="del"
                          title="确认删除该次分析？"
                          okText="删除"
                          cancelText="取消"
                          okButtonProps={{ danger: true }}
                          onConfirm={(e) => {
                            e?.stopPropagation()
                            onDelete(item.analysisId)
                          }}
                          onCancel={(e) => e?.stopPropagation()}>
                          <Button
                            type="text"
                            size="small"
                            danger
                            icon={<DeleteOutlined />}
                            loading={deletingId === item.analysisId}
                            onClick={(e) => e.stopPropagation()}
                          />
                        </Popconfirm>,
                      ]
                    : []
                }>
                <List.Item.Meta
                  avatar={
                    <FileTextOutlined
                      style={{ color: active ? '#6366f1' : '#94a3b8', fontSize: 18 }}
                    />
                  }
                  title={
                    <Space size={6}>
                      <Text strong style={{ fontSize: 12, color: '#0f172a' }}>
                        {item.analysisId}
                      </Text>
                      {active && <Tag color="blue">当前</Tag>}
                    </Space>
                  }
                  description={
                    <Space direction="vertical" size={2} style={{ width: '100%' }}>
                      <Text
                        type="secondary"
                        style={{ fontSize: 11 }}
                        ellipsis={{ tooltip: item.snippet }}>
                        {item.snippet || '（无摘要）'}
                      </Text>
                      <Text type="secondary" style={{ fontSize: 11 }}>
                        <ClockCircleOutlined style={{ marginRight: 4 }} />
                        {item.createdAt} · {item.tookMs}ms
                      </Text>
                    </Space>
                  }
                />
              </List.Item>
            )
          }}
        />
      )}
    </Card>
  )
}

export default IndustryAnalysisHistory
