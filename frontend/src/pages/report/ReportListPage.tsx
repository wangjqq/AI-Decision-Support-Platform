import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Table, Input, Space, Button, Tag, type TableProps } from 'antd'
import { SearchOutlined, EyeOutlined, ThunderboltOutlined } from '@ant-design/icons'
import { useGetReportsQuery } from '../../api/reportApi'
import { formatDateTime } from '../../utils/format'

interface ReportRow {
  id: number
  title: string
  type?: string
  status?: 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED'
  updatedAt?: string
}

const STATUS_COLOR: Record<NonNullable<ReportRow['status']>, string> = {
  PENDING: 'default',
  RUNNING: 'processing',
  SUCCESS: 'success',
  FAILED: 'error',
}

const STATUS_LABEL: Record<NonNullable<ReportRow['status']>, string> = {
  PENDING: '待处理',
  RUNNING: '生成中',
  SUCCESS: '已完成',
  FAILED: '失败',
}

/** 报告列表页：报告查询 + 触发 AI 生成 */
const ReportListPage = () => {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')

  const { data, isFetching, error } = useGetReportsQuery({ page: 1, size: 20, keyword })
  const list: ReportRow[] = (data as { list?: ReportRow[] } | undefined)?.list ?? []

  const columns: TableProps<ReportRow>['columns'] = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 100 },
    { title: '报告标题', dataIndex: 'title', key: 'title' },
    { title: '类型', dataIndex: 'type', key: 'type', width: 120 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (s: ReportRow['status']) => (s ? <Tag color={STATUS_COLOR[s]}>{STATUS_LABEL[s]}</Tag> : '-'),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      render: (v: string) => formatDateTime(v),
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_v, record) => (
        <Button type="link" icon={<EyeOutlined />} onClick={() => navigate(`/reports/${record.id}`)}>
          查看
        </Button>
      ),
    },
  ]

  return (
    <Card
      className="glass-card"
      title="报告列表"
      extra={
        <Space>
          <Input
            allowClear
            placeholder="搜索报告标题"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<ThunderboltOutlined />}>
            AI 生成报告
          </Button>
        </Space>
      }>
      <Table<ReportRow>
        rowKey="id"
        loading={isFetching}
        columns={columns}
        dataSource={list}
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `共 ${t} 条` }}
        locale={{ emptyText: error ? '后端暂未就绪或网络异常' : '暂无数据' }}
      />
    </Card>
  )
}

export default ReportListPage
