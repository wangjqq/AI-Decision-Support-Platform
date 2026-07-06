import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Button, Skeleton, Result, Space } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useGetIndustryByIdQuery } from '../../api/industryApi'
import { formatDateTime } from '../../utils/format'

/** 行业详情页：展示行业基础信息、描述、关联统计（占位） */
const IndustryDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const industryId = Number(id)

  const { data, isFetching, error } = useGetIndustryByIdQuery(industryId, {
    skip: !industryId || Number.isNaN(industryId),
  })

  const detail = (data ?? {}) as Record<string, unknown>

  return (
    <Card
      title={`行业详情 #${id}`}
      extra={
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/industries')}>
            返回列表
          </Button>
        </Space>
      }>
      {isFetching ? (
        <Skeleton active />
      ) : error ? (
        <Result status="warning" title="加载失败" subTitle="后端暂未就绪或网络异常" />
      ) : (
        <Descriptions column={2} bordered size="middle">
          <Descriptions.Item label="ID">{detail.id ?? industryId}</Descriptions.Item>
          <Descriptions.Item label="行业名称">{detail.name ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="行业编码">{detail.code ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="层级">{detail.level ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="行业描述" span={2}>
            {(detail.description as string) ?? '-'}
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">{formatDateTime(detail.createdAt as string)}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{formatDateTime(detail.updatedAt as string)}</Descriptions.Item>
        </Descriptions>
      )}
    </Card>
  )
}

export default IndustryDetailPage
