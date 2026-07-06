import { Button, Card, Empty, Spin, Alert, Space, Typography } from 'antd'
import { ThunderboltOutlined, ReloadOutlined } from '@ant-design/icons'
import CompanyAnalysisResultView from './CompanyAnalysisResultView'
import type { CompanyAnalysisResult } from '../../../api/companyApi'

const { Text } = Typography

export interface CompanyAnalysisPanelProps {
  /** 当前公司名（头部展示） */
  companyName?: string
  /** 当前结果 */
  result: CompanyAnalysisResult | null
  loading?: boolean
  error?: unknown
  /** 触发分析的回调 */
  onAnalyze?: () => void
  /** 当前是否正在分析（用于按钮 loading 态） */
  analyzing?: boolean
}

/**
 * 公司分析面板
 * - loading：Spin + "AI 编排中..."
 * - error：Alert
 * - 空态：Empty + 提示用户点"立即分析"
 * - 数据：渲染 5 维度结果
 */
const CompanyAnalysisPanel = ({
  companyName,
  result,
  loading,
  error,
  onAnalyze,
  analyzing,
}: CompanyAnalysisPanelProps) => {
  return (
    <Card
      className="glass-card"
      title={
        <Space>
          <ThunderboltOutlined style={{ color: '#6366f1' }} />
          <span className="gradient-text">AI 公司分析</span>
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
          <Spin size="large" tip={`正在分析 ${companyName ?? '公司'} ...`} />
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
              点击右上角"立即分析"，由 AI 对 <b>{companyName ?? '该公司'}</b> 进行 5 维度分析
            </Text>
          }
        />
      ) : (
        <CompanyAnalysisResultView data={result} />
      )}
    </Card>
  )
}

export default CompanyAnalysisPanel
