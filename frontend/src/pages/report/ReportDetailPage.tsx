import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Button, Skeleton, Result, Space, Tag } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useGetReportByIdQuery } from '../../api/reportApi'
import { formatDateTime } from '../../utils/format'

/** 报告详情页：基础信息 + 报告内容区（占位） */
const ReportDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const reportId = Number(id)

  const { data, isFetching, error } = useGetReportByIdQuery(reportId, {
    skip: !reportId || Number.isNaN(reportId),
  })

  const detail = (data ?? {}) as Record<string, unknown>

  return (
    <Card
      className="glass-card"
      title={`报告详情 #${id}`}
      extra={
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/reports')}>
            返回列表
          </Button>
        </Space>
      }>
      {isFetching ? (
        <Skeleton active />
      ) : error ? (
        <Result status="warning" title="加载失败" subTitle="后端暂未就绪或网络异常" />
      ) : (
        <>
          <Descriptions column={2} bordered size="middle">
            <Descriptions.Item label="ID">{detail.id ?? reportId}</Descriptions.Item>
            <Descriptions.Item label="报告标题">{detail.title ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="类型">{detail.type ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">
              {detail.status ? <Tag>{(detail.status as string).toUpperCase()}</Tag> : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">{formatDateTime(detail.createdAt as string)}</Descriptions.Item>
            <Descriptions.Item label="更新时间">{formatDateTime(detail.updatedAt as string)}</Descriptions.Item>
            <Descriptions.Item label="摘要" span={2}>
              {(detail.summary as string) ?? '-'}
            </Descriptions.Item>
          </Descriptions>

          <Card type="inner" title="报告内容" style={{ marginTop: 16 }}>
            <div style={{ minHeight: 200, color: 'rgba(0,0,0,0.45)' }}>
              报告正文加载区域（待集成富文本 / Markdown 渲染）
            </div>
          </Card>
        </>
      )}
    </Card>
  )
}

export default ReportDetailPage
