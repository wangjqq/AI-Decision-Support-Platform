import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Button, Space, Result, App, Row, Col, Tag } from 'antd'
import { ArrowLeftOutlined, BankOutlined } from '@ant-design/icons'
import {
  useGetCompanyByIdQuery,
  useGetCompanyAnalysisHistoryQuery,
  useGetCompanyAnalysisByIdQuery,
  useAnalyzeCompanyMutation,
  useDeleteCompanyAnalysisMutation,
  type CompanyAnalysisResult,
} from '../../api/companyApi'
import { useAppDispatch, useAppSelector } from '../../hooks/redux'
import { setCurrentAnalysis, clearCurrentAnalysis } from '../../stores/slices/companySlice'
import CompanyBasicCard from './components/CompanyBasicCard'
import CompanyAnalysisPanel from './components/CompanyAnalysisPanel'
import CompanyAnalysisHistory from './components/CompanyAnalysisHistory'

/** 公司详情页：左基本信息 + 历史 / 右 5 维度分析 */
const CompanyDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { message } = App.useApp()

  const companyId = Number(id)

  const [pickedAnalysisId, setPickedAnalysisId] = useState<string | null>(null)

  const {
    data: company,
    isFetching: companyLoading,
    error: companyError,
  } = useGetCompanyByIdQuery(companyId, { skip: !companyId || Number.isNaN(companyId) })

  const {
    data: historyResp,
    isFetching: historyLoading,
    refetch: refetchHistory,
  } = useGetCompanyAnalysisHistoryQuery(
    { id: companyId, page: 1, size: 20 },
    {
      skip: !companyId || Number.isNaN(companyId),
    },
  )

  // 点历史：拉取单条详情
  const {
    data: pickedDetail,
    isFetching: pickedLoading,
    error: pickedError,
  } = useGetCompanyAnalysisByIdQuery({ id: companyId, analysisId: pickedAnalysisId! }, { skip: !pickedAnalysisId })

  const [analyzeCompany, { isLoading: analyzing, error: analyzeError }] = useAnalyzeCompanyMutation()

  const [deleteAnalysis] = useDeleteCompanyAnalysisMutation()

  // 当前展示的 5 维度结果（优先级：当前结果 > picked 历史详情）
  const currentAnalysis = useAppSelector((s) => s.company.currentAnalysis)
  const displayedResult: CompanyAnalysisResult | null =
    currentAnalysis?.companyId === companyId ? currentAnalysis : ((pickedDetail as CompanyAnalysisResult) ?? null)
  const displayedAnalysisId = currentAnalysis?.companyId === companyId ? currentAnalysis.analysisId : pickedAnalysisId

  // 切换公司时清空
  useEffect(() => {
    dispatch(clearCurrentAnalysis())
    setPickedAnalysisId(null)
  }, [companyId, dispatch])

  // 拉取单条详情后写回 store
  useEffect(() => {
    if (pickedDetail && pickedDetail.companyId === companyId) {
      dispatch(setCurrentAnalysis(pickedDetail))
    }
  }, [pickedDetail, companyId, dispatch])

  const handleAnalyze = async () => {
    try {
      const result = await analyzeCompany({ id: companyId, body: {} }).unwrap()
      dispatch(setCurrentAnalysis(result))
      setPickedAnalysisId(null)
      message.success(`分析完成（${result.tookMs}ms）`)
      refetchHistory()
    } catch {
      // error 已被 baseQuery 拦截 toast
    }
  }

  const handlePickHistory = (analysisId: string) => {
    setPickedAnalysisId(analysisId)
  }

  const handleDeleteHistory = async (analysisId: string) => {
    try {
      await deleteAnalysis({ id: companyId, analysisId }).unwrap()
      message.success('已删除')
      if (currentAnalysis?.analysisId === analysisId) {
        dispatch(clearCurrentAnalysis())
      }
      if (pickedAnalysisId === analysisId) {
        setPickedAnalysisId(null)
      }
    } catch {
      // 错误已 toast
    }
  }

  if (!companyId || Number.isNaN(companyId)) {
    return (
      <Card className="glass-card">
        <Result status="warning" title="无效的公司 ID" />
      </Card>
    )
  }

  return (
    <div>
      {/* 顶部：标题 + 返回 + 立即分析 */}
      <Card
        bordered={false}
        style={{
          marginBottom: 16,
          background: 'linear-gradient(135deg, rgba(99,102,241,0.06), rgba(6,182,212,0.04))',
          borderRadius: 16,
        }}
        bodyStyle={{ padding: '14px 20px' }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/companies')}>
            返回列表
          </Button>
          <BankOutlined style={{ fontSize: 22, color: '#6366f1' }} />
          <span style={{ fontSize: 18, fontWeight: 600, color: '#0f172a' }}>{company?.name ?? '加载中...'}</span>
          {company?.code && <Tag color="geekblue">{company.code}</Tag>}
          {company?.industryName && <Tag color="blue">{company.industryName}</Tag>}
          {company?.industry && <Tag color="cyan">{company.industry}</Tag>}
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {/* 左栏：基本信息 + 历史 */}
        <Col xs={24} md={8}>
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <CompanyBasicCard company={company} loading={companyLoading} />
            <CompanyAnalysisHistory
              items={historyResp?.list ?? []}
              loading={historyLoading}
              currentAnalysisId={displayedAnalysisId}
              onPick={handlePickHistory}
              onDelete={handleDeleteHistory}
              deletingId={undefined}
            />
          </Space>
        </Col>

        {/* 右栏：5 维度分析面板 */}
        <Col xs={24} md={16}>
          <CompanyAnalysisPanel
            companyName={company?.name}
            result={displayedResult}
            loading={pickedLoading || analyzing}
            error={analyzeError || pickedError || companyError}
            onAnalyze={handleAnalyze}
            analyzing={analyzing}
          />
        </Col>
      </Row>
    </div>
  )
}

export default CompanyDetailPage
