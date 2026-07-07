import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Button, Space, App, Row, Col, Tag, Result } from 'antd'
import { ArrowLeftOutlined, ApartmentOutlined } from '@ant-design/icons'
import {
  useGetIndustryByIdQuery,
  useGetIndustryAnalysisHistoryQuery,
  useGetIndustryAnalysisByIdQuery,
  useAnalyzeIndustryMutation,
  useDeleteIndustryAnalysisMutation,
  type IndustryAnalysisResult,
} from '../../api/industryApi'
import { useAppDispatch, useAppSelector } from '../../hooks/redux'
import {
  setCurrentAnalysis,
  clearCurrentAnalysis,
} from '../../stores/slices/industrySlice'
import IndustryBasicCard from './components/IndustryBasicCard'
import IndustryAnalysisPanel from './components/IndustryAnalysisPanel'
import IndustryAnalysisHistory from './components/IndustryAnalysisHistory'

/** 行业详情页：左基本信息 + 历史 / 右 6 维度分析 + 产业链展示 */
const IndustryDetailPage = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const dispatch = useAppDispatch()
  const { message } = App.useApp()

  const industryId = Number(id)

  const [pickedAnalysisId, setPickedAnalysisId] = useState<string | null>(null)

  const {
    data: industry,
    isFetching: industryLoading,
    error: industryError,
  } = useGetIndustryByIdQuery(industryId, {
    skip: !industryId || Number.isNaN(industryId),
  })

  const {
    data: historyResp,
    isFetching: historyLoading,
    refetch: refetchHistory,
  } = useGetIndustryAnalysisHistoryQuery(
    { id: industryId, page: 1, size: 20 },
    {
      skip: !industryId || Number.isNaN(industryId),
    },
  )

  // 点历史：拉取单条详情
  const {
    data: pickedDetail,
    isFetching: pickedLoading,
    error: pickedError,
  } = useGetIndustryAnalysisByIdQuery(
    { id: industryId, analysisId: pickedAnalysisId! },
    { skip: !pickedAnalysisId },
  )

  const [analyzeIndustry, { isLoading: analyzing, error: analyzeError }] =
    useAnalyzeIndustryMutation()
  const [deleteAnalysis] = useDeleteIndustryAnalysisMutation()

  // 当前展示的 6 维度结果（优先级：当前 store 结果 > picked 历史详情）
  const currentAnalysis = useAppSelector((s) => s.industry.currentAnalysis)
  const displayedResult: IndustryAnalysisResult | null =
    currentAnalysis?.industryId === industryId
      ? currentAnalysis
      : ((pickedDetail as IndustryAnalysisResult) ?? null)
  const displayedAnalysisId =
    currentAnalysis?.industryId === industryId
      ? currentAnalysis.analysisId
      : pickedAnalysisId

  // 切换行业时清空
  useEffect(() => {
    dispatch(clearCurrentAnalysis())
    setPickedAnalysisId(null)
  }, [industryId, dispatch])

  // 拉取单条详情后写回 store
  useEffect(() => {
    if (pickedDetail && pickedDetail.industryId === industryId) {
      dispatch(setCurrentAnalysis(pickedDetail))
    }
  }, [pickedDetail, industryId, dispatch])

  const handleAnalyze = async () => {
    try {
      const result = await analyzeIndustry({ id: industryId, body: {} }).unwrap()
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
      await deleteAnalysis({ id: industryId, analysisId }).unwrap()
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

  if (!industryId || Number.isNaN(industryId)) {
    return (
      <Card className="glass-card">
        <Result status="warning" title="无效的行业 ID" />
      </Card>
    )
  }

  return (
    <div>
      {/* 顶部：标题 + 返回 */}
      <Card
        bordered={false}
        style={{
          marginBottom: 16,
          background: 'linear-gradient(135deg, rgba(99,102,241,0.06), rgba(6,182,212,0.04))',
          borderRadius: 16,
        }}
        bodyStyle={{ padding: '14px 20px' }}>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/industries')}>
            返回列表
          </Button>
          <ApartmentOutlined style={{ fontSize: 22, color: '#6366f1' }} />
          <span style={{ fontSize: 18, fontWeight: 600, color: '#0f172a' }}>
            {industry?.name ?? '加载中...'}
          </span>
          {industry?.code && <Tag color="default">{industry.code}</Tag>}
          {industry?.level ? <Tag color="blue">L{industry.level}</Tag> : null}
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {/* 左栏：基本信息 + 历史 */}
        <Col xs={24} md={8}>
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <IndustryBasicCard industry={industry} loading={industryLoading} />
            <IndustryAnalysisHistory
              items={historyResp?.list ?? []}
              loading={historyLoading}
              currentAnalysisId={displayedAnalysisId}
              onPick={handlePickHistory}
              onDelete={handleDeleteHistory}
            />
          </Space>
        </Col>

        {/* 右栏：6 维度分析面板 */}
        <Col xs={24} md={16}>
          <IndustryAnalysisPanel
            industryName={industry?.name}
            result={displayedResult}
            loading={pickedLoading || analyzing}
            error={analyzeError || pickedError || industryError}
            onAnalyze={handleAnalyze}
            analyzing={analyzing}
          />
        </Col>
      </Row>
    </div>
  )
}

export default IndustryDetailPage
