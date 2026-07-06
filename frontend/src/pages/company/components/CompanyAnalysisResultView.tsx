import { Space, Tag, Typography, Card } from 'antd'
import { ClockCircleOutlined, TagsOutlined } from '@ant-design/icons'
import { Row, Col } from 'antd'
import type { CompanyAnalysisResult } from '../../../api/companyApi'
import CompanyDimensionCard from './CompanyDimensionCard'

const { Text, Title } = Typography

export interface CompanyAnalysisResultViewProps {
  data: CompanyAnalysisResult | null
}

/**
 * 5 维度分析结果容器
 * - 头部：耗时 / analysisId
 * - 主体：2 列网格展示 5 个维度
 */
const CompanyAnalysisResultView = ({ data }: CompanyAnalysisResultViewProps) => {
  if (!data) {
    return null
  }
  return (
    <div>
      {/* 头部元信息 */}
      <Card
        bordered={false}
        size="small"
        style={{
          marginBottom: 16,
          background: 'linear-gradient(135deg, rgba(99,102,241,0.08), rgba(6,182,212,0.06))',
          borderRadius: 14,
        }}>
        <Space size={12} wrap>
          <Title level={5} style={{ margin: 0 }}>
            {data.companyName} · AI 分析
          </Title>
          <Tag color="blue">COMPANY</Tag>
          <Text type="secondary" style={{ fontSize: 12 }}>
            <ClockCircleOutlined style={{ marginRight: 4 }} />
            耗时 {data.tookMs} ms
          </Text>
          <Text type="secondary" style={{ fontSize: 12 }}>
            <TagsOutlined style={{ marginRight: 4 }} />
            ID: {data.analysisId}
          </Text>
        </Space>
      </Card>

      {/* 5 维度网格 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <CompanyDimensionCard data={data.overview} fallbackTitle="公司概览" />
        </Col>
        <Col xs={24} md={12}>
          <CompanyDimensionCard data={data.mainBusiness} fallbackTitle="主营业务" />
        </Col>
        <Col xs={24} md={12}>
          <CompanyDimensionCard data={data.advantages} fallbackTitle="核心优势" />
        </Col>
        <Col xs={24} md={12}>
          <CompanyDimensionCard data={data.risks} fallbackTitle="潜在风险" />
        </Col>
        <Col xs={24}>
          <CompanyDimensionCard data={data.aiConclusion} fallbackTitle="AI 结论" />
        </Col>
      </Row>
    </div>
  )
}

export default CompanyAnalysisResultView
