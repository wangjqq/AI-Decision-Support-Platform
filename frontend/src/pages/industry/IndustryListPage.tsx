import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Table, Input, Space, Button, type TableProps } from 'antd'
import { SearchOutlined, EyeOutlined } from '@ant-design/icons'
import { useGetIndustriesQuery } from '../../api/industryApi'
import { formatDateTime } from '../../utils/format'

interface IndustryRow {
  id: number
  name: string
  code?: string
  level?: number
  updatedAt?: string
}

/** 行业列表页：行业基础信息查询 + 关键字搜索 */
const IndustryListPage = () => {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')

  const { data, isFetching, error } = useGetIndustriesQuery({ page: 1, size: 20, keyword })
  const list: IndustryRow[] = (data as { list?: IndustryRow[] } | undefined)?.list ?? []

  const columns: TableProps<IndustryRow>['columns'] = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 100 },
    { title: '行业名称', dataIndex: 'name', key: 'name' },
    { title: '行业编码', dataIndex: 'code', key: 'code' },
    { title: '层级', dataIndex: 'level', key: 'level', width: 100 },
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
        <Button type="link" icon={<EyeOutlined />} onClick={() => navigate(`/industries/${record.id}`)}>
          查看
        </Button>
      ),
    },
  ]

  return (
    <Card
      className="glass-card"
      title="行业列表"
      extra={
        <Space>
          <Input
            allowClear
            placeholder="搜索行业名称"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 240 }}
          />
          <Button type="primary">新增行业</Button>
        </Space>
      }>
      <Table<IndustryRow>
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

export default IndustryListPage
