import { Card, Descriptions, Skeleton, Space, Statistic, Tag, Typography } from 'antd'
import { formatDate } from '../../../utils/format'
import type { CompanyVO } from '../../../api/companyApi'

const { Title, Text } = Typography

export interface CompanyBasicCardProps {
  company?: CompanyVO
  loading?: boolean
}

/** 将数字格式化为"x亿 / x万"（无单位后缀） */
const formatAmount = (v?: number) => {
  if (v == null) return '-'
  if (v >= 100_000_000) return `${(v / 100_000_000).toFixed(2)} 亿`
  if (v >= 10_000) return `${(v / 10_000).toFixed(2)} 万`
  return String(v)
}

/**
 * 公司基本信息卡片（基于 Descriptions）
 * <p>展示股票代码、细分行业、业务板块、财务核心指标等真实企业数据。
 */
const CompanyBasicCard = ({ company, loading }: CompanyBasicCardProps) => {
  if (loading) {
    return (
      <Card className="glass-card" title="公司基本信息">
        <Skeleton active />
      </Card>
    )
  }
  if (!company) {
    return (
      <Card className="glass-card" title="公司基本信息">
        <Text type="secondary">未找到公司</Text>
      </Card>
    )
  }
  return (
    <Card
      className="glass-card"
      title={
        <span>
          <span className="gradient-text">公司基本信息</span>
        </span>
      }>
      <Title level={4} style={{ marginTop: 0, marginBottom: 12 }}>
        {company.name}
        {company.code && (
          <Tag color="geekblue" style={{ marginLeft: 8, fontWeight: 500 }}>
            {company.code}
          </Tag>
        )}
      </Title>

      {/* 财务核心指标 */}
      {company.financial && (company.financial.revenue != null || company.financial.profit != null) && (
        <Space size="large" style={{ width: '100%', marginBottom: 16 }} wrap>
          <Statistic
            title="营收"
            value={formatAmount(company.financial.revenue)}
            valueStyle={{ fontSize: 20, color: '#1677ff' }}
            suffix={
              company.financial.period ? (
                <Text type="secondary" style={{ fontSize: 12 }}>
                  （{company.financial.period}）
                </Text>
              ) : null
            }
          />
          <Statistic
            title="净利润"
            value={formatAmount(company.financial.profit)}
            valueStyle={{
              fontSize: 20,
              color: (company.financial.profit ?? 0) >= 0 ? '#52c41a' : '#ff4d4f',
            }}
          />
        </Space>
      )}

      <Descriptions column={1} size="small" bordered>
        <Descriptions.Item label="ID">{company.id}</Descriptions.Item>
        <Descriptions.Item label="股票代码">{company.code || '-'}</Descriptions.Item>
        <Descriptions.Item label="统一社会信用代码">{company.uscc || '-'}</Descriptions.Item>
        <Descriptions.Item label="所属行业">{company.industryName || '-'}</Descriptions.Item>
        <Descriptions.Item label="细分行业">{company.industry || '-'}</Descriptions.Item>
        <Descriptions.Item label="主营业务">{company.mainBusiness || '-'}</Descriptions.Item>
        <Descriptions.Item label="业务板块">
          {company.business && company.business.length > 0 ? (
            <Space size={[4, 4]} wrap>
              {company.business.map((b) => (
                <Tag key={b} color="cyan">
                  {b}
                </Tag>
              ))}
            </Space>
          ) : (
            '-'
          )}
        </Descriptions.Item>
        <Descriptions.Item label="成立日期">{formatDate(company.establishedAt)}</Descriptions.Item>
        <Descriptions.Item label="注册地址">{company.address || '-'}</Descriptions.Item>
        <Descriptions.Item label="公司简介">{company.description || '-'}</Descriptions.Item>
        <Descriptions.Item label="更新时间">{company.updatedAt || '-'}</Descriptions.Item>
      </Descriptions>
    </Card>
  )
}

export default CompanyBasicCard
