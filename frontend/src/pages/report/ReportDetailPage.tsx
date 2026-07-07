import { useEffect, useMemo, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Button,
  Skeleton,
  Result,
  Space,
  Tag,
  Typography,
  Anchor,
  Empty,
  Popconfirm,
  App,
  Divider,
  Drawer,
} from 'antd'
import {
  ArrowLeftOutlined,
  FileTextOutlined,
  RobotOutlined,
  DownloadOutlined,
  DeleteOutlined,
  MenuOutlined,
  LinkOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeSlug from 'rehype-slug'
import { useGetReportByIdQuery, useDeleteReportMutation } from '../../api/reportApi'
import { formatDateTime } from '../../utils/format'

const { Title, Paragraph, Text } = Typography

const STATUS_COLOR: Record<string, string> = {
  PENDING: 'default',
  RUNNING: 'processing',
  SUCCESS: 'success',
  FAILED: 'error',
}

const STATUS_LABEL: Record<string, string> = {
  PENDING: '待处理',
  RUNNING: '生成中',
  SUCCESS: '已完成',
  FAILED: '失败',
}

const TYPE_LABEL: Record<string, string> = {
  COMPANY: '公司画像',
  INDUSTRY: '行业研究',
  COMPREHENSIVE: '综合研报',
}

const TYPE_COLOR: Record<string, string> = {
  COMPANY: 'blue',
  INDUSTRY: 'purple',
  COMPREHENSIVE: 'magenta',
}

const REFERENCE_SOURCE_LABEL: Record<string, string> = {
  INDUSTRY: '行业',
  COMPANY: '公司',
  POLICY: '政策',
  NEWS: '新闻',
}

const REFERENCE_SOURCE_COLOR: Record<string, string> = {
  INDUSTRY: 'purple',
  COMPANY: 'blue',
  POLICY: 'orange',
  NEWS: 'cyan',
}

/** 报告详情页：左侧目录导航 + 右侧 Markdown 渲染正文 */
const ReportDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { message } = App.useApp()
  const [tocDrawerOpen, setTocDrawerOpen] = useState(false)

  const reportId = id ?? ''
  const { data, isFetching, error } = useGetReportByIdQuery(reportId, {
    skip: !reportId,
  })

  const [deleteReport, { isLoading: deleting }] = useDeleteReportMutation()

  // 选中目录项时，关闭移动端 Drawer
  const handleAnchorClick = () => {
    setTocDrawerOpen(false)
  }

  const handleDelete = async () => {
    try {
      await deleteReport(reportId).unwrap()
      message.success('已删除')
      navigate('/reports')
    } catch {
      // 已 toast
    }
  }

  const handleDownload = () => {
    if (!data) {
      return
    }
    const blob = new Blob([data.markdown ?? ''], { type: 'text/markdown;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${data.title ?? 'report'}-${data.reportId}.md`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  // 自动滚动到顶部
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'auto' })
  }, [reportId])

  const tocItems = useMemo(() => {
    if (!data?.toc) {
      return []
    }
    return data.toc.map((t) => ({
      key: t.anchor,
      href: `#${t.anchor}`,
      title: t.title,
    }))
  }, [data?.toc])

  if (isFetching) {
    return (
      <Card className="glass-card">
        <Skeleton active paragraph={{ rows: 12 }} />
      </Card>
    )
  }

  if (error || !data) {
    return (
      <Card className="glass-card">
        <Result
          status="warning"
          title="报告加载失败"
          subTitle="报告不存在或后端暂未就绪"
          extra={
            <Button type="primary" onClick={() => navigate('/reports')}>
              返回报告列表
            </Button>
          }
        />
      </Card>
    )
  }

  return (
    <div>
      {/* 顶部 Banner */}
      <Card
        bordered={false}
        style={{
          marginBottom: 16,
          background: 'linear-gradient(135deg, rgba(99,102,241,0.10), rgba(6,182,212,0.06))',
          borderRadius: 16,
        }}
        bodyStyle={{ padding: '20px 24px' }}>
        <Space direction="vertical" size={8} style={{ width: '100%' }}>
          <Space wrap>
            <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/reports')}>
              返回列表
            </Button>
            <Button
              icon={<MenuOutlined />}
              onClick={() => setTocDrawerOpen(true)}
              className="report-toc-drawer-trigger">
              目录
            </Button>
            <Button icon={<DownloadOutlined />} onClick={handleDownload}>
              下载 Markdown
            </Button>
            <Popconfirm
              title="确认删除此报告？"
              okText="删除"
              cancelText="取消"
              okButtonProps={{ danger: true }}
              onConfirm={handleDelete}>
              <Button danger icon={<DeleteOutlined />} loading={deleting}>
                删除
              </Button>
            </Popconfirm>
          </Space>

          <Space size={12} align="start" wrap>
            <FileTextOutlined style={{ fontSize: 28, color: '#6366f1' }} />
            <div>
              <Title level={3} style={{ margin: 0, color: '#0f172a' }}>
                {data.title}
              </Title>
              <Space size={6} wrap style={{ marginTop: 6 }}>
                <Tag color={TYPE_COLOR[data.type] ?? 'default'}>{TYPE_LABEL[data.type] ?? data.type}</Tag>
                <Tag color={STATUS_COLOR[data.status] ?? 'default'}>
                  {STATUS_LABEL[data.status] ?? data.status}
                </Tag>
                <Tag color="blue">{data.companyName}</Tag>
                {data.industryName && <Tag color="purple">{data.industryName}</Tag>}
                <Text type="secondary" style={{ fontSize: 12 }}>
                  <ClockCircleOutlined style={{ marginRight: 4 }} />
                  生成耗时 {data.tookMs} ms
                </Text>
                <Text type="secondary" style={{ fontSize: 12 }}>
                  创建于 {formatDateTime(data.createdAt)}
                </Text>
              </Space>
            </div>
          </Space>
        </Space>
      </Card>

      <div className="report-layout">
        {/* 左侧目录（PC 端） */}
        <aside className="report-toc-sidebar">
          <Card className="glass-card" size="small" title={<Space><RobotOutlined />报告目录</Space>}>
            {tocItems.length > 0 ? (
              <Anchor
                items={tocItems}
                affix={false}
                targetOffset={80}
                onClick={handleAnchorClick}
              />
            ) : (
              <Empty description="暂无目录" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}

            {data.references && data.references.length > 0 && (
              <>
                <Divider style={{ margin: '12px 0' }} />
                <div style={{ fontSize: 12, color: '#94a3b8', marginBottom: 8 }}>
                  <LinkOutlined style={{ marginRight: 4 }} />
                  参考资料（{data.references.length}）
                </div>
                <Space direction="vertical" size={6} style={{ width: '100%' }}>
                  {data.references.slice(0, 6).map((ref, i) => (
                    <a
                      key={i}
                      href={ref.url || '#'}
                      target="_blank"
                      rel="noreferrer"
                      style={{ fontSize: 12, lineHeight: 1.5 }}>
                      <Tag color={REFERENCE_SOURCE_COLOR[ref.sourceType ?? ''] ?? 'default'} style={{ marginRight: 4 }}>
                        {REFERENCE_SOURCE_LABEL[ref.sourceType ?? ''] ?? '参考'}
                      </Tag>
                      {ref.title}
                    </a>
                  ))}
                </Space>
              </>
            )}
          </Card>
        </aside>

        {/* 移动端目录 Drawer */}
        <Drawer
          title="报告目录"
          placement="left"
          open={tocDrawerOpen}
          onClose={() => setTocDrawerOpen(false)}
          width={280}>
          {tocItems.length > 0 ? (
            <Anchor items={tocItems} affix={false} onClick={handleAnchorClick} />
          ) : (
            <Empty description="暂无目录" image={Empty.PRESENTED_IMAGE_SIMPLE} />
          )}
        </Drawer>

        {/* 右侧 Markdown 正文 */}
        <main className="report-content">
          <Card className="glass-card" bodyStyle={{ padding: '24px 32px' }}>
            {data.summary && (
              <div
                style={{
                  background: 'linear-gradient(135deg, rgba(99,102,241,0.06), rgba(6,182,212,0.04))',
                  border: '1px solid rgba(99,102,241,0.15)',
                  borderRadius: 10,
                  padding: '16px 20px',
                  marginBottom: 24,
                }}>
                <Space style={{ marginBottom: 8 }} size={6}>
                  <RobotOutlined style={{ color: '#6366f1' }} />
                  <Text strong style={{ color: '#6366f1' }}>摘要</Text>
                </Space>
                <Paragraph style={{ marginBottom: 0, color: '#475569' }}>
                  {data.summary}
                </Paragraph>
              </div>
            )}

            <article className="markdown-body">
              <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                rehypePlugins={[rehypeSlug]}
                components={{
                  h1: ({ children, ...props }) => (
                    <h1 id={(props as { id?: string }).id} style={markdownH1Style}>
                      {children}
                    </h1>
                  ),
                  h2: ({ children, ...props }) => (
                    <h2 id={(props as { id?: string }).id} style={markdownH2Style}>
                      {children}
                    </h2>
                  ),
                  h3: ({ children, ...props }) => (
                    <h3 id={(props as { id?: string }).id} style={markdownH3Style}>
                      {children}
                    </h3>
                  ),
                  table: ({ children }) => (
                    <div style={{ overflowX: 'auto', margin: '12px 0' }}>
                      <table style={markdownTableStyle}>{children}</table>
                    </div>
                  ),
                  th: ({ children }) => <th style={markdownThStyle}>{children}</th>,
                  td: ({ children }) => <td style={markdownTdStyle}>{children}</td>,
                  blockquote: ({ children }) => <blockquote style={markdownBlockquoteStyle}>{children}</blockquote>,
                  a: ({ children, href }) => (
                    <a href={href} target="_blank" rel="noreferrer" style={{ color: '#6366f1' }}>
                      {children}
                    </a>
                  ),
                }}>
                {data.markdown}
              </ReactMarkdown>
            </article>
          </Card>
        </main>
      </div>
    </div>
  )
}

// ===================== Markdown 样式 =====================

const markdownH1Style: React.CSSProperties = {
  fontSize: 26,
  fontWeight: 700,
  margin: '8px 0 16px',
  paddingBottom: 12,
  borderBottom: '1px solid rgba(99,102,241,0.15)',
  color: '#0f172a',
}

const markdownH2Style: React.CSSProperties = {
  fontSize: 20,
  fontWeight: 700,
  margin: '24px 0 12px',
  paddingBottom: 8,
  borderBottom: '1px solid rgba(99,102,241,0.08)',
  color: '#1e293b',
  scrollMarginTop: 80,
}

const markdownH3Style: React.CSSProperties = {
  fontSize: 16,
  fontWeight: 600,
  margin: '16px 0 8px',
  color: '#334155',
  scrollMarginTop: 80,
}

const markdownTableStyle: React.CSSProperties = {
  width: '100%',
  borderCollapse: 'collapse',
  fontSize: 13,
  background: 'rgba(255,255,255,0.6)',
  borderRadius: 8,
  overflow: 'hidden',
}

const markdownThStyle: React.CSSProperties = {
  padding: '8px 12px',
  background: 'rgba(99,102,241,0.08)',
  borderBottom: '1px solid rgba(99,102,241,0.15)',
  textAlign: 'left',
  fontWeight: 600,
  color: '#0f172a',
}

const markdownTdStyle: React.CSSProperties = {
  padding: '8px 12px',
  borderBottom: '1px solid rgba(99,102,241,0.06)',
  color: '#334155',
}

const markdownBlockquoteStyle: React.CSSProperties = {
  margin: '12px 0',
  padding: '8px 16px',
  background: 'rgba(6,182,212,0.05)',
  borderLeft: '3px solid #06b6d4',
  color: '#475569',
  borderRadius: '0 6px 6px 0',
}

export default ReportDetailPage
