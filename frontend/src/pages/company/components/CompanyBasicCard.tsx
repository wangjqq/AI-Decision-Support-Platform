import { Card, Descriptions, Skeleton, Typography } from 'antd'
import { formatDate } from '../../../utils/format'
import type { CompanyVO } from '../../../api/companyApi'

const { Title, Text } = Typography

export interface CompanyBasicCardProps {
  company?: CompanyVO
  loading?: boolean
}

/**
 * 公司基本信息卡片（基于 Descriptions）
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
      </Title>
      <Descriptions column={1} size="small" bordered>
        <Descriptions.Item label="ID">{company.id}</Descriptions.Item>
        <Descriptions.Item label="统一社会信用代码">
          {company.uscc || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="所属行业">
          {company.industryName || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="主营业务">{company.mainBusiness || '-'}</Descriptions.Item>
        <Descriptions.Item label="成立日期">
          {formatDate(company.establishedAt)}
        </Descriptions.Item>
        <Descriptions.Item label="注册地址">{company.address || '-'}</Descriptions.Item>
        <Descriptions.Item label="公司简介">
          {company.description || '-'}
        </Descriptions.Item>
        <Descriptions.Item label="更新时间">
          {company.updatedAt || '-'}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  )
}

export default CompanyBasicCard
