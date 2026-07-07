import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card,
  Table,
  Input,
  Space,
  Button,
  Tag,
  Modal,
  Form,
  Select,
  App,
  Tooltip,
  Popconfirm,
  type TableProps,
} from 'antd'
import {
  SearchOutlined,
  EyeOutlined,
  ThunderboltOutlined,
  RobotOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons'
import {
  useGetReportsQuery,
  useGenerateReportMutation,
  useDeleteReportMutation,
  type ReportHistoryItem,
} from '../../api/reportApi'
import { useGetCompaniesQuery } from '../../api/companyApi'
import {
  useAnalyzeCompanyMutation,
  useGetCompanyAnalysisHistoryQuery,
  useGetCompanyAnalysisByIdQuery,
} from '../../api/companyApi'
import { formatDateTime } from '../../utils/format'

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

/** 报告列表页：报告查询 + 触发 AI 生成 + 删除 */
const ReportListPage = () => {
  const navigate = useNavigate()
  const { message } = App.useApp()
  const [keyword, setKeyword] = useState('')
  const [generateOpen, setGenerateOpen] = useState(false)

  const { data, isFetching, refetch } = useGetReportsQuery({ page: 1, size: 20, keyword })
  const list: ReportHistoryItem[] = (data as { list?: ReportHistoryItem[] } | undefined)?.list ?? []

  const [generateReport, { isLoading: generating }] = useGenerateReportMutation()
  const [deleteReport, { isLoading: deleting }] = useDeleteReportMutation()

  const handleGenerate = async (values: {
    companyId: number
    companyName?: string
    companyAnalysisId: string
    companyAnalysis: import('../../api/reportApi').ReportGenerateRequest['companyAnalysis']
    title?: string
  }) => {
    try {
      const result = await generateReport({
        companyId: values.companyId,
        companyName: values.companyName,
        companyAnalysisId: values.companyAnalysisId,
        companyAnalysis: values.companyAnalysis,
        title: values.title,
      }).unwrap()
      message.success(`报告生成完成（${result.tookMs}ms）`)
      setGenerateOpen(false)
      refetch()
      navigate(`/reports/${result.reportId}`)
    } catch {
      // 错误已由 baseQuery toast
    }
  }

  const handleDelete = async (reportId: string) => {
    try {
      await deleteReport(reportId).unwrap()
      message.success('已删除')
    } catch {
      // 错误已 toast
    }
  }

  const columns: TableProps<ReportHistoryItem>['columns'] = [
    { title: 'ID', dataIndex: 'reportId', key: 'reportId', width: 200, ellipsis: true },
    {
      title: '报告标题',
      dataIndex: 'title',
      key: 'title',
      render: (v: string, r) => (
        <a onClick={() => navigate(`/reports/${r.reportId}`)} style={{ fontWeight: 500 }}>
          {v}
        </a>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (v: string) => <Tag color={TYPE_COLOR[v] ?? 'default'}>{TYPE_LABEL[v] ?? v}</Tag>,
    },
    {
      title: '所属',
      key: 'subject',
      width: 200,
      render: (_v, r) => (
        <Space size={4} wrap>
          <Tag color="blue">{r.companyName}</Tag>
          {r.industryName && <Tag color="purple">{r.industryName}</Tag>}
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 90,
      render: (s: string) => <Tag color={STATUS_COLOR[s] ?? 'default'}>{STATUS_LABEL[s] ?? s}</Tag>,
    },
    {
      title: '摘要',
      dataIndex: 'summary',
      key: 'summary',
      ellipsis: true,
      render: (v?: string) => v || '-',
    },
    {
      title: '耗时',
      dataIndex: 'tookMs',
      key: 'tookMs',
      width: 90,
      render: (v: number) => `${v} ms`,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 170,
      render: (v: string) => formatDateTime(v),
    },
    {
      title: '操作',
      key: 'action',
      width: 160,
      fixed: 'right',
      render: (_v, record) => (
        <Space>
          <Tooltip title="查看">
            <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/reports/${record.reportId}`)}>
              查看
            </Button>
          </Tooltip>
          <Popconfirm
            title="确认删除报告？"
            okText="删除"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record.reportId)}>
            <Tooltip title="删除">
              <Button type="link" size="small" danger icon={<DeleteOutlined />} loading={deleting}>
                删除
              </Button>
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <>
      <Card
        className="glass-card"
        title="报告列表"
        extra={
          <Space>
            <Input
              allowClear
              placeholder="搜索报告标题 / 公司 / 行业"
              prefix={<SearchOutlined />}
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              style={{ width: 260 }}
            />
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              刷新
            </Button>
            <Button type="primary" icon={<ThunderboltOutlined />} onClick={() => setGenerateOpen(true)}>
              AI 生成报告
            </Button>
          </Space>
        }>
        <Table<ReportHistoryItem>
          rowKey="reportId"
          loading={isFetching}
          columns={columns}
          dataSource={list}
          scroll={{ x: 1200 }}
          pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `共 ${t} 条` }}
          locale={{ emptyText: '暂无报告，点击右上角"AI 生成报告"开始' }}
        />
      </Card>

      <GenerateReportModal
        open={generateOpen}
        loading={generating}
        onCancel={() => setGenerateOpen(false)}
        onSubmit={handleGenerate}
      />
    </>
  )
}

// ===================== 生成报告弹窗 =====================

interface GenerateModalProps {
  open: boolean
  loading: boolean
  onCancel: () => void
  onSubmit: (values: {
    companyId: number
    companyName?: string
    companyAnalysisId: string
    companyAnalysis: import('../../api/reportApi').ReportGenerateRequest['companyAnalysis']
    title?: string
  }) => Promise<void> | void
}

/**
 * 报告生成弹窗：选公司 -> 选历史分析 -> 可选报告标题 -> 提交。
 * <p>后端走同步生成（1.5-2.5s），返回完整 Report VO，前端跳详情页。
 */
const GenerateReportModal = ({ open, loading, onCancel, onSubmit }: GenerateModalProps) => {
  const [form] = Form.useForm<{ companyId: number; companyAnalysisId: string; title?: string }>()
  const companyId = Form.useWatch('companyId', form)

  // 拉取公司列表（用于选择公司）
  const { data: companyResp } = useGetCompaniesQuery({ page: 1, size: 200 })
  const companyOptions = useMemo(
    () =>
      ((companyResp as { list?: { id: number; name: string; industryName?: string }[] } | undefined)?.list ?? []).map(
        (c) => ({
          label: c.industryName ? `${c.name} · ${c.industryName}` : c.name,
          value: c.id,
        }),
      ),
    [companyResp],
  )

  // 拉取所选公司的分析历史
  const { data: historyResp, isFetching: historyLoading } = useGetCompanyAnalysisHistoryQuery(
    { id: companyId, page: 1, size: 20 },
    { skip: !companyId },
  )
  const historyList = (historyResp as { list?: { analysisId: string; createdAt: string; tookMs: number }[] } | undefined)
    ?.list ?? []

  // 当选择某条历史后，拉取完整分析（用于校验 / 透传）
  const pickedAnalysisId = Form.useWatch('companyAnalysisId', form)
  const { data: pickedDetail, isFetching: pickedLoading } = useGetCompanyAnalysisByIdQuery(
    { id: companyId, analysisId: pickedAnalysisId ?? '' },
    { skip: !companyId || !pickedAnalysisId },
  )

  const [analyzeCompany, { isLoading: triggering }] = useAnalyzeCompanyMutation()

  const handleTrigger = async () => {
    if (!companyId) {
      return
    }
    try {
      const result = await analyzeCompany({ id: companyId, body: {} }).unwrap()
      form.setFieldsValue({ companyAnalysisId: result.analysisId })
    } catch {
      // 已 toast
    }
  }

  const handleOk = async () => {
    const values = await form.validateFields()
    if (!pickedDetail) {
      // eslint-disable-next-line no-alert
      alert('请先选择一条公司分析')
      return
    }
    // 从 companyOptions 中查出 companyName
    const companyLabel = companyOptions.find((o) => o.value === values.companyId)?.label as string | undefined
    await onSubmit({
      companyId: values.companyId,
      companyName: companyLabel,
      companyAnalysisId: values.companyAnalysisId,
      companyAnalysis: pickedDetail,
      title: values.title,
    })
  }

  return (
    <Modal
      title={
        <Space>
          <RobotOutlined style={{ color: '#6366f1' }} />
          <span>AI 生成研究报告</span>
        </Space>
      }
      open={open}
      onCancel={onCancel}
      onOk={handleOk}
      confirmLoading={loading}
      okText="开始生成"
      cancelText="取消"
      width={560}
      destroyOnClose>
      <Form form={form} layout="vertical" preserve={false} style={{ marginTop: 16 }}>
        <Form.Item
          name="companyId"
          label="选择公司"
          rules={[{ required: true, message: '请选择目标公司' }]}>
          <Select
            showSearch
            placeholder="搜索并选择目标公司"
            options={companyOptions}
            filterOption={(input, option) =>
              (option?.label as string)?.toLowerCase().includes(input.toLowerCase())
            }
            onChange={() => form.setFieldsValue({ companyAnalysisId: undefined })}
          />
        </Form.Item>

        <Form.Item
          name="companyAnalysisId"
          label="选择公司分析（将作为报告输入）"
          rules={[{ required: true, message: '请选择一条公司分析' }]}
          extra={
            <Space size={4} style={{ marginTop: 6 }}>
              <span style={{ color: '#94a3b8', fontSize: 12 }}>
                若无可用分析，请先触发一次分析
              </span>
              <Button
                size="small"
                type="link"
                icon={<ThunderboltOutlined />}
                loading={triggering}
                disabled={!companyId}
                onClick={handleTrigger}>
                立即分析
              </Button>
            </Space>
          }>
          <Select
            placeholder={companyId ? '请选择历史分析记录' : '请先选择公司'}
            loading={historyLoading || pickedLoading}
            disabled={!companyId}
            options={historyList.map((h) => ({
              label: `${h.analysisId} · ${formatDateTime(h.createdAt)} · ${h.tookMs}ms`,
              value: h.analysisId,
            }))}
            notFoundContent={companyId ? '该暂无分析记录，请先点击"立即分析"' : '请先选择公司'}
          />
        </Form.Item>

        {pickedDetail && (
          <div
            style={{
              padding: '8px 12px',
              background: 'rgba(99,102,241,0.06)',
              border: '1px solid rgba(99,102,241,0.15)',
              borderRadius: 8,
              marginBottom: 16,
              fontSize: 12,
              color: '#475569',
            }}>
            已选分析：<b>{pickedDetail.analysisId}</b>（{formatDateTime(pickedDetail.createdAt)}）
          </div>
        )}

        <Form.Item name="title" label="报告标题（可选，留空由 AI 自动生成）">
          <Input placeholder="如：宁德时代 · 锂离子电池行业 研究报告" maxLength={200} showCount />
        </Form.Item>

        <div
          style={{
            fontSize: 12,
            color: '#94a3b8',
            background: 'rgba(6,182,212,0.04)',
            padding: '8px 12px',
            borderRadius: 6,
            border: '1px dashed rgba(6,182,212,0.25)',
          }}>
          <RobotOutlined style={{ marginRight: 6, color: '#06b6d4' }} />
          报告将由 AI Agent 同步生成（耗时约 1.5-2.5 秒），生成后自动跳转到详情页。
        </div>
      </Form>
    </Modal>
  )
}

export default ReportListPage
