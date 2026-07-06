import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Table, Input, Space, Button, type TableProps } from 'antd'
import { SearchOutlined, EyeOutlined } from '@ant-design/icons'
import { useGetCompaniesQuery } from '../../api/companyApi'
import { formatDateTime } from '../../utils/format'

interface CompanyRow {
  id: number
  name: string
  industryName?: string
  updatedAt?: string
}

/** 公司列表页：表格 + 顶部搜索框，操作列跳详情（占位数据） */
const CompanyListPage = () => {
  const navigate = useNavigate()
  const [keyword, setKeyword] = useState('')

  // 调用 RTK Query；后端未就绪时静默处理（占位 skeleton）
  const { data, isFetching, error } = useGetCompaniesQuery({ page: 1, size: 20, keyword })

  // 后端尚未就绪时显示占位空数据
  const list: CompanyRow[] = (data as { list?: CompanyRow[] } | undefined)?.list ?? []

  const columns: TableProps<CompanyRow>['columns'] = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 100 },
    { title: '公司名称', dataIndex: 'name', key: 'name' },
    { title: '所属行业', dataIndex: 'industryName', key: 'industryName' },
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
        <Button type="link" icon={<EyeOutlined />} onClick={() => navigate(`/companies/${record.id}`)}>
          查看
        </Button>
      ),
    },
  ]

  return (
    <Card
      title="公司列表"
      extra={
        <Space>
          <Input
            allowClear
            placeholder="搜索公司名称"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 240 }}
          />
          <Button type="primary">新增公司</Button>
        </Space>
      }>
      <Table<CompanyRow>
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

export default CompanyListPage
