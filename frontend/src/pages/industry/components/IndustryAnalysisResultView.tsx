import { Card, Space, Tag, Typography } from 'antd'
import { ClockCircleOutlined, TagsOutlined } from '@ant-design/icons'
import { Row, Col } from 'antd'
import type { IndustryAnalysisResult } from '../../../api/industryApi'
import IndustryDimensionCard from './IndustryDimensionCard'

const { Text, Title } = Typography

export interface IndustryAnalysisResultViewProps {
  data: IndustryAnalysisResult | null
}

/**
 * 6 维度分析结果容器
 * - 头部：耗时 / analysisId
 * - 主体：2 列网格展示 6 个维度
 */
const IndustryAnalysisResultView = ({ data }: IndustryAnalysisResultViewProps) => {
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
            {data.industryName} · AI 行业分析
          </Title>
          <Tag color="purple">INDUSTRY</Tag>
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

      {/* 6 维度网格 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} md={12}>
          <IndustryDimensionCard data={data.overview} fallbackTitle="行业概况" />
        </Col>
        <Col xs={24} md={12}>
          <IndustryDimensionCard data={data.marketSize} fallbackTitle="市场空间" />
        </Col>
        <Col xs={24} md={12}>
          <IndustryDimensionCard data={data.chain} fallbackTitle="产业链结构" />
        </Col>
        <Col xs={24} md={12}>
          <IndustryDimensionCard data={data.leading} fallbackTitle="龙头企业" />
        </Col>
        <Col xs={24} md={12}>
          <IndustryDimensionCard data={data.trends} fallbackTitle="未来趋势" />
        </Col>
        <Col xs={24} md={12}>
          <IndustryDimensionCard data={data.risks} fallbackTitle="风险分析" />
        </Col>
      </Row>
    </div>
  )
}

export default IndustryAnalysisResultView
