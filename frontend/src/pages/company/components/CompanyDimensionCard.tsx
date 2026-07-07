import { Card, Typography, List, Tag, Space } from 'antd'
import {
  BankOutlined,
  AppstoreOutlined,
  TrophyOutlined,
  WarningOutlined,
  RobotOutlined,
  ThunderboltOutlined,
  AimOutlined,
  BulbOutlined,
  FileTextOutlined,
} from '@ant-design/icons'
import type { ComponentProps, ComponentType } from 'react'
import type { CompanyDimension } from '../../../api/companyApi'

const { Title, Paragraph, Text } = Typography

type IconComponent = ComponentType<ComponentProps<typeof BankOutlined>>

/** icon name → Ant Design 组件 */
const ICON_MAP: Record<string, IconComponent> = {
  BankOutlined,
  AppstoreOutlined,
  TrophyOutlined,
  WarningOutlined,
  RobotOutlined,
  ThunderboltOutlined,
  AimOutlined,
  BulbOutlined,
  FileTextOutlined,
}

/** color → 渐变背景色 */
const COLOR_GRADIENT: Record<string, string> = {
  blue: 'linear-gradient(135deg, #6366f1, #3b82f6)',
  green: 'linear-gradient(135deg, #10b981, #34d399)',
  gold: 'linear-gradient(135deg, #f59e0b, #fbbf24)',
  orange: 'linear-gradient(135deg, #f97316, #fb923c)',
  red: 'linear-gradient(135deg, #ef4444, #f87171)',
  purple: 'linear-gradient(135deg, #8b5cf6, #a78bfa)',
}

export interface CompanyDimensionCardProps {
  data: CompanyDimension | null | undefined
  /** 备用标题（data.title 为空时显示） */
  fallbackTitle?: string
}

/**
 * 公司分析单一维度卡片
 * - 头部：图标 + 标题 + 主题色
 * - 主体：summary（可复制）+ keyPoints 列表
 */
const CompanyDimensionCard = ({ data, fallbackTitle }: CompanyDimensionCardProps) => {
  if (!data) {
    return null
  }
  const title = data.title || fallbackTitle || '-'
  const iconName = data.icon ?? 'FileTextOutlined'
  const Icon = ICON_MAP[iconName] ?? FileTextOutlined
  const gradient = COLOR_GRADIENT[data.color ?? 'blue'] ?? COLOR_GRADIENT.blue
  const keyPoints = Array.isArray(data.keyPoints) ? data.keyPoints : []
  const metrics = data.metrics ?? {}

  return (
    <Card
      bordered={false}
      style={{
        borderRadius: 16,
        background: 'rgba(255,255,255,0.6)',
        backdropFilter: 'blur(12px) saturate(180%)',
        boxShadow: '0 4px 18px rgba(99,102,241,0.08)',
      }}
      bodyStyle={{ padding: 18 }}>
      {/* 头部 */}
      <Space size={10} align="center" style={{ marginBottom: 12 }}>
        <div
          style={{
            width: 36,
            height: 36,
            borderRadius: 10,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            background: gradient,
            color: '#fff',
            fontSize: 18,
            boxShadow: '0 4px 12px rgba(99,102,241,0.25)',
          }}>
          <Icon />
        </div>
        <Title level={5} style={{ margin: 0, color: '#0f172a' }}>
          {title}
        </Title>
        {data.color && (
          <Tag color={data.color} style={{ marginLeft: 'auto' }}>
            {data.color}
          </Tag>
        )}
      </Space>

      {/* 摘要 */}
      {data.summary && (
        <Paragraph
          copyable={{ tooltips: ['复制摘要', '已复制'] }}
          style={{
            fontSize: 13,
            color: '#475569',
            lineHeight: 1.7,
            marginBottom: keyPoints.length > 0 ? 12 : 0,
            whiteSpace: 'pre-wrap',
          }}>
          {data.summary}
        </Paragraph>
      )}

      {/* 要点 */}
      {keyPoints.length > 0 && (
        <List
          size="small"
          split={false}
          dataSource={keyPoints}
          renderItem={(p, i) => (
            <List.Item style={{ padding: '6px 0', border: 'none' }}>
              <Space size={8} align="start">
                <Text
                  style={{
                    color: '#6366f1',
                    fontWeight: 700,
                    fontSize: 13,
                    minWidth: 18,
                  }}>
                  {i + 1}.
                </Text>
                <Text style={{ color: '#0f172a', fontSize: 13, lineHeight: 1.7 }}>{p}</Text>
              </Space>
            </List.Item>
          )}
        />
      )}

      {/* 指标（如有） */}
      {Object.keys(metrics).length > 0 && (
        <div
          style={{
            marginTop: 8,
            padding: '8px 12px',
            borderRadius: 10,
            background: 'rgba(99,102,241,0.06)',
          }}>
          {Object.entries(metrics).map(([k, v]) => (
            <div key={k} style={{ fontSize: 12, color: '#475569', marginBottom: 2 }}>
              <Text type="secondary">{k}:</Text> <Text strong>{v}</Text>
            </div>
          ))}
        </div>
      )}
    </Card>
  )
}

export default CompanyDimensionCard
