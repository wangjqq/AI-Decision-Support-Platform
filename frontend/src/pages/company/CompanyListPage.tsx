import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Table, Input, Space, Button, Popconfirm, Tag, Typography, type TableProps, App } from 'antd'
import {
  SearchOutlined,
  EyeOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons'
import { useDeleteCompanyMutation, useGetCompaniesQuery, type CompanyVO } from '../../api/companyApi'
import { formatDateTime } from '../../utils/format'
import { useAppDispatch } from '../../hooks/redux'
import { openCreateEditor, openEditEditor } from '../../stores/slices/companySlice'
import CompanyCreateModal from './components/CompanyCreateModal'

/** 公司列表页：表格 + 顶部搜索框 + 增/改/删（接入 RTK Query 强类型） */
const CompanyListPage = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { message } = App.useApp()
  const { Text } = Typography

  const [page, setPage] = useState(1)
  const [size, setSize] = useState(20)
  const [keyword, setKeyword] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')

  const { data, isFetching, refetch } = useGetCompaniesQuery({ page, size, keyword: searchKeyword })
  const [deleteCompany, { isLoading: deleting }] = useDeleteCompanyMutation()

  const list: CompanyVO[] = data?.list ?? []
  const total = data?.total ?? 0

  const handleDelete = async (id: number) => {
    try {
      await deleteCompany(id).unwrap()
      message.success('已删除')
    } catch {
      // 错误已由 baseQuery 拦截 toast
    }
  }

  const columns: TableProps<CompanyVO>['columns'] = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 70 },
    {
      title: '股票代码',
      dataIndex: 'code',
      key: 'code',
      width: 100,
      render: (v: string | undefined) =>
        v ? <Tag color="geekblue">{v}</Tag> : <Text type="secondary">-</Text>,
    },
    { title: '公司名称', dataIndex: 'name', key: 'name', ellipsis: true },
    {
      title: '细分行业',
      dataIndex: 'industry',
      key: 'industry',
      width: 120,
      render: (v: string | undefined) => v ?? '-',
    },
    {
      title: '所属行业',
      dataIndex: 'industryName',
      key: 'industryName',
      width: 110,
      render: (v: string | undefined) => v ?? '-',
    },
    {
      title: '主营营收',
      dataIndex: ['financial', 'revenue'],
      key: 'revenue',
      width: 130,
      render: (_v, record) => {
        const r = record.financial?.revenue
        if (r == null) return '-'
        if (r >= 100_000_000) return `${(r / 100_000_000).toFixed(2)} 亿`
        if (r >= 10_000) return `${(r / 10_000).toFixed(2)} 万`
        return String(r)
      },
    },
    { title: '主营业务', dataIndex: 'mainBusiness', key: 'mainBusiness', ellipsis: true },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 170,
      render: (v: string | undefined) => formatDateTime(v),
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
      render: (_v, record) => (
        <Space size={4}>
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/companies/${record.id}`)}>
            查看
          </Button>
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => dispatch(openEditEditor(record))}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该公司？"
            description="删除后不可恢复，相关分析记录也会失效"
            okText="删除"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />} loading={deleting}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <Card
      className="glass-card"
      title="公司列表"
      extra={
        <Space>
          <Input
            allowClear
            placeholder="搜索公司名称"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={() => {
              setSearchKeyword(keyword)
              setPage(1)
            }}
            style={{ width: 240 }}
          />
          <Button
            icon={<SearchOutlined />}
            onClick={() => {
              setSearchKeyword(keyword)
              setPage(1)
            }}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />} onClick={() => refetch()} />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => dispatch(openCreateEditor())}>
            新增公司
          </Button>
        </Space>
      }>
      <Table<CompanyVO>
        rowKey="id"
        loading={isFetching}
        columns={columns}
        dataSource={list}
        scroll={{ x: 1300 }}
        pagination={{
          current: page,
          pageSize: size,
          total,
          showSizeChanger: true,
          showTotal: (t) => `共 ${t} 条`,
          onChange: (p, s) => {
            setPage(p)
            setSize(s)
          },
        }}
        locale={{ emptyText: '暂无数据' }}
      />

      {/* 新增/编辑 Modal（受 store 全局控制） */}
      <CompanyCreateModal />
    </Card>
  )
}

export default CompanyListPage
