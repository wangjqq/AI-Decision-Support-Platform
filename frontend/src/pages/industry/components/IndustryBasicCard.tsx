import { Card, Descriptions, Skeleton, Tag, Typography } from 'antd'
import { formatDateTime } from '../../../utils/format'
import type { IndustryVO } from '../../../api/industryApi'

const { Title, Text } = Typography

export interface IndustryBasicCardProps {
  industry?: IndustryVO
  loading?: boolean
}

/** 层级中文名 */
const LEVEL_LABEL: Record<number, string> = {
  1: '门类',
  2: '大类',
  3: '中类',
  4: '小类',
}

/**
 * 行业基本信息卡片（基于 Descriptions）
 */
const IndustryBasicCard = ({ industry, loading }: IndustryBasicCardProps) => {
  if (loading) {
    return (
      <Card className="glass-card" title="行业基本信息">
        <Skeleton active />
      </Card>
    )
  }
  if (!industry) {
    return (
      <Card className="glass-card" title="行业基本信息">
        <Text type="secondary">未找到行业</Text>
      </Card>
    )
  }
  const tags = (industry.tags ?? '')
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean)
  return (
    <Card
      className="glass-card"
      title={
        <span>
          <span className="gradient-text">行业基本信息</span>
        </span>
      }>
      <Title level={4} style={{ marginTop: 0, marginBottom: 12 }}>
        {industry.name}
        {industry.level ? (
          <Tag color="blue" style={{ marginLeft: 8, fontSize: 12 }}>
            {LEVEL_LABEL[industry.level] ?? `Level ${industry.level}`}
          </Tag>
        ) : null}
      </Title>
      <Descriptions column={1} size="small" bordered>
        <Descriptions.Item label="行业编码">{industry.code || '-'}</Descriptions.Item>
        <Descriptions.Item label="所属门类/父行业">
          {industry.parentName || (industry.parentId ? `#${industry.parentId}` : '顶级门类')}
        </Descriptions.Item>
        <Descriptions.Item label="行业描述">{industry.description || '-'}</Descriptions.Item>
        <Descriptions.Item label="行业标签">
          {tags.length > 0 ? tags.map((t) => <Tag key={t}>{t}</Tag>) : '-'}
        </Descriptions.Item>
        <Descriptions.Item label="更新时间">
          {formatDateTime(industry.updatedAt)}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  )
}

export default IndustryBasicCard
