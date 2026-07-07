import { Card, Empty, List, Space, Tag, Typography } from 'antd'
import { LinkOutlined } from '@ant-design/icons'
import type { IndustryAnalysisReference } from '../../../api/industryApi'

const { Text } = Typography

export interface IndustryReferencesCardProps {
  refs?: IndustryAnalysisReference[]
  loading?: boolean
}

/**
 * 行业分析引用参考列表
 */
const IndustryReferencesCard = ({ refs, loading }: IndustryReferencesCardProps) => {
  const list = Array.isArray(refs) ? refs : []
  return (
    <Card
      className="glass-card"
      title={
        <Space>
          <LinkOutlined style={{ color: '#06b6d4' }} />
          <span className="gradient-text">分析参考</span>
        </Space>
      }>
      {loading ? (
        <Text type="secondary">加载中...</Text>
      ) : list.length === 0 ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={<Text type="secondary">暂无引用参考</Text>}
        />
      ) : (
        <List
          size="small"
          split={false}
          dataSource={list}
          renderItem={(r) => (
            <List.Item style={{ padding: '8px 0', border: 'none' }}>
              <Space direction="vertical" size={2} style={{ width: '100%' }}>
                <Space size={6} wrap>
                  <Text strong style={{ fontSize: 13, color: '#0f172a' }}>
                    {r.title}
                  </Text>
                  {r.url && (
                    <a href={r.url} target="_blank" rel="noreferrer" style={{ fontSize: 11 }}>
                      <LinkOutlined /> 链接
                    </a>
                  )}
                  <Tag color="cyan" style={{ fontSize: 10 }}>
                    参考
                  </Tag>
                </Space>
                {r.snippet && (
                  <Text type="secondary" style={{ fontSize: 12, lineHeight: 1.6 }}>
                    {r.snippet}
                  </Text>
                )}
              </Space>
            </List.Item>
          )}
        />
      )}
    </Card>
  )
}

export default IndustryReferencesCard
