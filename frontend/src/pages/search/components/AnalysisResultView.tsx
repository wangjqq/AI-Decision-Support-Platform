import {
  Spin,
  Alert,
  Empty,
  Typography,
  Descriptions,
  List,
  Tag,
  Space,
  Divider,
} from 'antd'
import { FileSearchOutlined, FileTextOutlined } from '@ant-design/icons'
import type { AnalysisResultDTO, AnalysisType, AnalysisReference } from '../../../api/analysisApi'

const { Title, Paragraph, Text } = Typography

/** 分析类型 → Tag 颜色 */
const TYPE_COLOR: Record<AnalysisType, string> = {
  COMPANY: 'blue',
  INDUSTRY: 'green',
  REPORT: 'purple',
}

/** 分析类型 → 中文标签 */
const TYPE_LABEL: Record<AnalysisType, string> = {
  COMPANY: '公司分析',
  INDUSTRY: '行业分析',
  REPORT: '报告分析',
}

export interface AnalysisResultViewProps {
  /** 当前分析结果（null 表示尚未发起/已清空） */
  data: AnalysisResultDTO | null
  /** 是否正在编排（来自 usePostQueryMutation.isLoading） */
  loading: boolean
  /** 错误对象（来自 usePostQueryMutation.error） */
  error?: unknown
}

/**
 * 分析结果展示组件
 * - loading：Spin + "AI 编排中..."
 * - error：Alert
 * - 空态：Empty
 * - 数据：Tag(类型) + 耗时 + 摘要（copyable） + 指标（Descriptions） + 核心要点 + 引用参考
 */
const AnalysisResultView = ({ data, loading, error }: AnalysisResultViewProps) => {
  if (loading) {
    return (
      <div
        style={{
          textAlign: 'center',
          padding: '60px 0',
        }}>
        <Spin size="large" tip="AI 编排中..." />
      </div>
    )
  }

  if (error) {
    return (
      <Alert
        type="error"
        showIcon
        message="分析失败"
        description="请稍后重试，或换个查询条件再试一次"
      />
    )
  }

  if (!data) {
    return (
      <Empty
        image={Empty.PRESENTED_IMAGE_SIMPLE}
        description="请输入查询条件，开始一次 AI 智能分析"
      />
    )
  }

  const { analysisType, result, tookMs, queryId } = data
  const safeResult = result ?? { summary: '', keyPoints: [], metrics: {}, references: [] }
  const summary: string = safeResult.summary ?? ''
  const keyPoints: string[] = Array.isArray(safeResult.keyPoints) ? safeResult.keyPoints : []
  const metrics: Record<string, string | number> = safeResult.metrics ?? {}
  const references: AnalysisReference[] = Array.isArray(safeResult.references)
    ? safeResult.references
    : []
  const metricEntries = Object.entries(metrics)

  return (
    <div>
      {/* 头部：类型 + 耗时 + queryId */}
      <Space size={8} wrap style={{ marginBottom: 12 }}>
        <Tag color={TYPE_COLOR[analysisType] ?? 'default'}>{TYPE_LABEL[analysisType] ?? analysisType}</Tag>
        {typeof tookMs === 'number' && (
          <Text type="secondary" style={{ fontSize: 12 }}>
            耗时 {tookMs} ms
          </Text>
        )}
        <Text type="secondary" style={{ fontSize: 12 }}>
          ID: {queryId}
        </Text>
      </Space>

      {/* 摘要 */}
      <Title level={5} style={{ marginTop: 0, marginBottom: 8 }}>
        <span className="gradient-text">分析摘要</span>
      </Title>
      {summary ? (
        <Paragraph
          copyable={{ tooltips: ['复制摘要', '已复制'] }}
          style={{ fontSize: 14, color: '#0f172a', whiteSpace: 'pre-wrap' }}>
          {summary}
        </Paragraph>
      ) : (
        <Text type="secondary">（暂无摘要）</Text>
      )}

      {/* 关键指标 */}
      {metricEntries.length > 0 && (
        <>
          <Divider style={{ margin: '12px 0' }} />
          <Title level={5} style={{ marginTop: 0, marginBottom: 8 }}>
            <span className="gradient-text">关键指标</span>
          </Title>
          <Descriptions column={2} bordered size="small">
            {metricEntries.map(([k, v]) => (
              <Descriptions.Item key={k} label={k}>
                {String(v)}
              </Descriptions.Item>
            ))}
          </Descriptions>
        </>
      )}

      {/* 核心要点 */}
      {keyPoints.length > 0 && (
        <>
          <Divider style={{ margin: '12px 0' }} />
          <Title level={5} style={{ marginTop: 0, marginBottom: 8 }}>
            <span className="gradient-text">核心要点</span>
          </Title>
          <ul style={{ paddingLeft: 22, margin: 0, color: '#0f172a' }}>
            {keyPoints.map((p, i) => (
              <li key={i} style={{ marginBottom: 6, lineHeight: 1.7 }}>
                {p}
              </li>
            ))}
          </ul>
        </>
      )}

      {/* 引用参考 */}
      {references.length > 0 && (
        <>
          <Divider style={{ margin: '12px 0' }} />
          <Title level={5} style={{ marginTop: 0, marginBottom: 8 }}>
            <span className="gradient-text">引用参考</span>
          </Title>
          <List<AnalysisReference>
            size="small"
            dataSource={references}
            renderItem={(r) => (
              <List.Item style={{ padding: '8px 12px' }}>
                <Space size={8} wrap>
                  <FileSearchOutlined style={{ color: '#6366f1' }} />
                  <Tag color={TYPE_COLOR[r.type] ?? 'default'} style={{ marginRight: 0 }}>
                    {TYPE_LABEL[r.type] ?? r.type}
                  </Tag>
                  <Text strong>{r.title}</Text>
                  {r.snippet && (
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {r.snippet}
                    </Text>
                  )}
                </Space>
              </List.Item>
            )}
          />
        </>
      )}

      {metricEntries.length === 0 && keyPoints.length === 0 && references.length === 0 && (
        <div style={{ marginTop: 16, textAlign: 'center' }}>
          <FileTextOutlined style={{ fontSize: 32, color: '#94a3b8' }} />
          <div style={{ color: '#94a3b8', fontSize: 12, marginTop: 8 }}>
            该次结果未返回结构化数据
          </div>
        </div>
      )}
    </div>
  )
}

export default AnalysisResultView
