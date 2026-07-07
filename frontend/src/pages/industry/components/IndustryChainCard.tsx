import { Card, Empty, List, Progress, Space, Tag, Typography } from 'antd'
import { BranchesOutlined, CrownOutlined } from '@ant-design/icons'
import type {
  IndustryChainNode,
  IndustryLeadingCompany,
} from '../../../api/industryApi'

const { Title, Text } = Typography

export interface IndustryChainCardProps {
  nodes?: IndustryChainNode[]
  summary?: string
  loading?: boolean
}

const TYPE_META: Record<string, { label: string; color: string }> = {
  UPSTREAM: { label: '上游', color: 'orange' },
  MIDSTREAM: { label: '中游', color: 'blue' },
  DOWNSTREAM: { label: '下游', color: 'green' },
}

/**
 * 产业链结构卡片（上 / 中 / 下游节点）
 */
const IndustryChainCard = ({ nodes, summary, loading }: IndustryChainCardProps) => {
  const list = Array.isArray(nodes) ? nodes : []
  return (
    <Card
      className="glass-card"
      title={
        <Space>
          <BranchesOutlined style={{ color: '#8b5cf6' }} />
          <span className="gradient-text">产业链结构</span>
        </Space>
      }>
      {loading ? (
        <Text type="secondary">加载中...</Text>
      ) : list.length === 0 ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<Text type="secondary">暂无产业链数据</Text>}
        />
      ) : (
        <>
          {summary && (
            <Text
              type="secondary"
              style={{
                display: 'block',
                marginBottom: 12,
                fontSize: 13,
                lineHeight: 1.7,
                whiteSpace: 'pre-wrap',
              }}>
              {summary}
            </Text>
          )}
          <List
            size="small"
            split={false}
            dataSource={list}
            renderItem={(n) => {
              const meta = TYPE_META[n.type ?? ''] ?? { label: n.type ?? '其他', color: 'default' }
              return (
                <List.Item
                  style={{
                    padding: '12px 14px',
                    marginBottom: 8,
                    borderRadius: 12,
                    background: 'rgba(99,102,241,0.04)',
                    border: '1px solid rgba(99,102,241,0.08)',
                  }}>
                  <List.Item.Meta
                    title={
                      <Space size={8}>
                        <Tag color={meta.color}>{meta.label}</Tag>
                        <Text strong style={{ fontSize: 13, color: '#0f172a' }}>
                          {n.name}
                        </Text>
                      </Space>
                    }
                    description={
                      <Space direction="vertical" size={2} style={{ width: '100%' }}>
                        {n.description && (
                          <Text style={{ fontSize: 12, color: '#475569' }}>{n.description}</Text>
                        )}
                        {n.representatives && (
                          <Text type="secondary" style={{ fontSize: 11 }}>
                            代表企业 / 细分：{n.representatives}
                          </Text>
                        )}
                      </Space>
                    }
                  />
                </List.Item>
              )
            }}
          />
        </>
      )}
    </Card>
  )
}

export default IndustryChainCard

export interface IndustryLeadingCardProps {
  companies?: IndustryLeadingCompany[]
  summary?: string
  loading?: boolean
}

/**
 * 龙头企业卡片
 */
export const IndustryLeadingCard = ({ companies, summary, loading }: IndustryLeadingCardProps) => {
  const list = Array.isArray(companies) ? companies : []
  return (
    <Card
      className="glass-card"
      title={
        <Space>
          <CrownOutlined style={{ color: '#f59e0b' }} />
          <span className="gradient-text">龙头企业</span>
        </Space>
      }>
      {loading ? (
        <Text type="secondary">加载中...</Text>
      ) : list.length === 0 ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<Text type="secondary">暂无龙头企业数据</Text>}
        />
      ) : (
        <>
          {summary && (
            <Text
              type="secondary"
              style={{
                display: 'block',
                marginBottom: 12,
                fontSize: 13,
                lineHeight: 1.7,
                whiteSpace: 'pre-wrap',
              }}>
              {summary}
            </Text>
          )}
          <List
            size="small"
            split={false}
            dataSource={list}
            renderItem={(c) => (
              <List.Item
                style={{
                  padding: '10px 12px',
                  marginBottom: 6,
                  borderRadius: 10,
                  background: 'rgba(245,158,11,0.06)',
                  border: '1px solid rgba(245,158,11,0.12)',
                }}>
                <List.Item.Meta
                  title={
                    <Space size={8} wrap>
                      <Text strong style={{ fontSize: 13, color: '#0f172a' }}>
                        {c.name}
                      </Text>
                      {c.stockCode && (
                        <Tag color="default" style={{ fontSize: 10 }}>
                          {c.stockCode}
                        </Tag>
                      )}
                      {c.tag && <Tag color="gold">{c.tag}</Tag>}
                    </Space>
                  }
                  description={
                    <Space direction="vertical" size={4} style={{ width: '100%' }}>
                      {c.description && (
                        <Text style={{ fontSize: 12, color: '#475569' }}>{c.description}</Text>
                      )}
                      {typeof c.marketShare === 'number' && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          <Text type="secondary" style={{ fontSize: 11, minWidth: 60 }}>
                            市占率
                          </Text>
                          <Progress
                            percent={Math.min(100, Math.max(0, c.marketShare))}
                            size="small"
                            showInfo
                            strokeColor={{ '0%': '#f59e0b', '100%': '#f97316' }}
                            style={{ flex: 1, marginBottom: 0 }}
                          />
                        </div>
                      )}
                    </Space>
                  }
                />
              </List.Item>
            )}
          />
        </>
      )}
    </Card>
  )
}

export const IndustryLeadingCardTitle = Title
