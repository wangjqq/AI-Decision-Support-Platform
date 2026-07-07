import { Button, Card, Empty, Spin, Alert, Space, Typography } from 'antd'
import { ThunderboltOutlined, ReloadOutlined } from '@ant-design/icons'
import IndustryAnalysisResultView from './IndustryAnalysisResultView'
import type { IndustryAnalysisResult } from '../../../api/industryApi'

const { Text } = Typography

export interface IndustryAnalysisPanelProps {
  industryName?: string
  result: IndustryAnalysisResult | null
  loading?: boolean
  error?: unknown
  onAnalyze?: () => void
  analyzing?: boolean
}

/**
 * 行业分析面板
 * - loading：Spin + "AI 编排中..."
 * - error：Alert
 * - 空态：Empty + 提示用户点"立即分析"
 * - 数据：渲染 6 维度结果
 */
const IndustryAnalysisPanel = ({
  industryName,
  result,
  loading,
  error,
  onAnalyze,
  analyzing,
}: IndustryAnalysisPanelProps) => {
  return (
    <Card
      className="glass-card"
      title={
        <Space>
          <ThunderboltOutlined style={{ color: '#6366f1' }} />
          <span className="gradient-text">AI 行业分析</span>
        </Space>
      }
      extra={
        <Button
          type="primary"
          icon={<ReloadOutlined spin={analyzing} />}
          onClick={onAnalyze}
          loading={analyzing}
          disabled={loading}>
          {result ? '重新分析' : '立即分析'}
        </Button>
      }>
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0' }}>
          <Spin size="large" tip={`正在分析 ${industryName ?? '行业'} ...`} />
        </div>
      ) : error ? (
        <Alert
          type="error"
          showIcon
          message="分析失败"
          description="请稍后重试，或联系管理员查看后端日志"
        />
      ) : !result ? (
        <Empty
          image={Empty.PRESENTED_IMAGE_SIMPLE}
          description={
            <Text type="secondary">
              点击右上角"立即分析"，由 AI 对 <b>{industryName ?? '该行业'}</b> 进行 6 维度分析
            </Text>
          }
        />
      ) : (
        <IndustryAnalysisResultView data={result} />
      )}
    </Card>
  )
}

export default IndustryAnalysisPanel
