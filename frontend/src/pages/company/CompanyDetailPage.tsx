import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Button, Skeleton, Result, Space } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useGetCompanyByIdQuery } from '../../api/companyApi'
import { formatDateTime } from '../../utils/format'

/** 公司详情页：根据路由参数 :id 拉取详情数据 */
const CompanyDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const companyId = Number(id)

  const { data, isFetching, error } = useGetCompanyByIdQuery(companyId, {
    skip: !companyId || Number.isNaN(companyId),
  })

  const detail = (data ?? {}) as Record<string, unknown>

  return (
    <Card
      title={`公司详情 #${id}`}
      extra={
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/companies')}>
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
          <Descriptions.Item label="ID">{detail.id ?? companyId}</Descriptions.Item>
          <Descriptions.Item label="公司名称">{detail.name ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="统一社会信用代码">{detail.uscc ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="所属行业">{(detail.industryName as string) ?? '-'}</Descriptions.Item>
          <Descriptions.Item label="主营业务" span={2}>
            {(detail.mainBusiness as string) ?? '-'}
          </Descriptions.Item>
          <Descriptions.Item label="创建时间">{formatDateTime(detail.createdAt as string)}</Descriptions.Item>
          <Descriptions.Item label="更新时间">{formatDateTime(detail.updatedAt as string)}</Descriptions.Item>
        </Descriptions>
      )}
    </Card>
  )
}

export default CompanyDetailPage
