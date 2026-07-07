import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { App, Card, Table, Input, Space, Button, Tag, Popconfirm, type TableProps } from 'antd'
import {
  SearchOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
} from '@ant-design/icons'
import {
  useGetIndustriesQuery,
  useDeleteIndustryMutation,
  type IndustryVO,
} from '../../api/industryApi'
import { formatDateTime } from '../../utils/format'
import { openCreateEditor, openEditEditor } from '../../stores/slices/industrySlice'
import { useAppDispatch } from '../../hooks/redux'
import IndustryCreateModal from './components/IndustryCreateModal'

/** 层级中文名 */
const LEVEL_LABEL: Record<number, string> = {
  1: '门类',
  2: '大类',
  3: '中类',
  4: '小类',
}

/** 行业列表页：行业基础信息查询 + 关键字搜索 + 新增/编辑/删除 */
const IndustryListPage = () => {
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { message, modal } = App.useApp()
  const [keyword, setKeyword] = useState('')
  const [deletingId, setDeletingId] = useState<number | null>(null)

  const { data, isFetching, error } = useGetIndustriesQuery({ page: 1, size: 20, keyword })
  const [deleteIndustry] = useDeleteIndustryMutation()

  const list: IndustryVO[] = (data as { list?: IndustryVO[] } | undefined)?.list ?? []

  const handleDelete = (record: IndustryVO) => {
    modal.confirm({
      title: '确认删除该行业？',
      content: `行业【${record.name}】删除后不可恢复。`,
      okText: '删除',
      cancelText: '取消',
      okButtonProps: { danger: true },
      onOk: async () => {
        setDeletingId(record.id)
        try {
          await deleteIndustry(record.id).unwrap()
          message.success('已删除')
        } catch {
          // error 已被 baseQuery 拦截 toast
        } finally {
          setDeletingId(null)
        }
      },
    })
  }

  const columns: TableProps<IndustryVO>['columns'] = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '行业名称', dataIndex: 'name', key: 'name' },
    { title: '行业编码', dataIndex: 'code', key: 'code', width: 120 },
    {
      title: '层级',
      dataIndex: 'level',
      key: 'level',
      width: 100,
      render: (v: number) =>
        v ? <Tag color="blue">{LEVEL_LABEL[v] ?? `Level ${v}`}</Tag> : '-',
    },
    {
      title: '父行业',
      key: 'parent',
      width: 160,
      render: (_v, record) =>
        record.parentName || (record.parentId ? `#${record.parentId}` : '顶级门类'),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180,
      render: (v: string) => formatDateTime(v),
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
      render: (_v, record) => (
        <Space size={4}>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/industries/${record.id}`)}>
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => dispatch(openEditEditor(record))}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该行业？"
            okText="删除"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDelete(record)}>
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
              loading={deletingId === record.id}>
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
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => dispatch(openCreateEditor())}>
            新增行业
          </Button>
        </Space>
      }>
      <Table<IndustryVO>
        rowKey="id"
        loading={isFetching}
        columns={columns}
        dataSource={list}
        scroll={{ x: 960 }}
        pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (t) => `共 ${t} 条` }}
        locale={{ emptyText: error ? '后端暂未就绪或网络异常' : '暂无数据' }}
      />
      <IndustryCreateModal />
    </Card>
  )
}

export default IndustryListPage
