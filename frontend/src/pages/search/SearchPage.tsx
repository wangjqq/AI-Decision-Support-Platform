import { useCallback, useMemo } from 'react'
import { Row, Col, Card, Spin, Typography, Space, Button, message as antdMessage } from 'antd'
import { ClearOutlined, ThunderboltFilled } from '@ant-design/icons'
import { useAppDispatch, useAppSelector } from '../../hooks/redux'
import {
  setInput,
  pushHistory,
  setCurrentResult,
  clearHistory,
} from '../../stores/slices/searchSlice'
import { usePostQueryMutation } from '../../api/analysisApi'
import type { HistoryItem, AnalysisType } from '../../api/analysisApi'
import SearchInput from './components/SearchInput'
import HistoryList from './components/HistoryList'
import AnalysisResultView from './components/AnalysisResultView'

const { Title, Text } = Typography

/** 页面最大宽度，保持与其他业务页一致 */
const CONTAINER_MAX_WIDTH = 1200

/**
 * 智能分析页（/search）
 * - 顶部：搜索输入
 * - 左：历史列表（localStorage 持久化）
 * - 右：分析结果展示
 * - 整体外层 Spin（提交时全屏遮罩）
 */
const SearchPage = () => {
  const dispatch = useAppDispatch()
  const inputValue = useAppSelector((s) => s.search.inputValue)
  const history = useAppSelector((s) => s.search.history)
  const currentResult = useAppSelector((s) => s.search.currentResult)

  const [postQuery, { isLoading, error }] = usePostQueryMutation()

  /** 提交分析查询 */
  const handleSubmit = useCallback(
    async (q: string) => {
      const trimmed = q.trim()
      if (!trimmed) {
        antdMessage.warning('请输入查询内容')
        return
      }
      try {
        const result = await postQuery({ query: trimmed }).unwrap()
        dispatch(setCurrentResult(result))
        const historyItem: HistoryItem = {
          queryId: result.queryId,
          query: trimmed,
          analysisType: (result.analysisType ?? 'COMPANY') as AnalysisType,
          tookMs: result.tookMs,
          createdAt: result.createdAt,
        }
        dispatch(pushHistory(historyItem))
      } catch {
        // 错误提示由 baseQuery / axios 拦截器统一弹出 message.error；此处不再 toast
      }
    },
    [dispatch, postQuery],
  )

  /** 点击历史项：回填输入框 + 自动重新提交 */
  const handlePick = useCallback(
    (item: HistoryItem) => {
      dispatch(setInput(item.query))
      // 使用 setTimeout 0 把 setInput 推到下一帧，避免 Input.Search 的 onChange 还没读到
      // 这里直接调用 handleSubmit，因为 inputValue 是 props 控制的，UI 视觉同步由 setInput 完成
      void handleSubmit(item.query)
    },
    [dispatch, handleSubmit],
  )

  /** 卡片标题区域 */
  const headerNode = useMemo(
    () => (
      <Space align="center" size={10}>
        <span
          className="icon-badge"
          style={{ width: 36, height: 36, fontSize: 18, borderRadius: 10 }}>
          <ThunderboltFilled />
        </span>
        <span>
          <div style={{ fontSize: 18, fontWeight: 700 }}>
            <span className="gradient-text">智能分析</span>
          </div>
          <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>
            AI Decision Support · 智能编排
          </div>
        </span>
      </Space>
    ),
    [],
  )

  return (
    <Spin
      spinning={isLoading}
      tip="AI 编排中..."
      size="large"
      style={{ maxWidth: CONTAINER_MAX_WIDTH, margin: '0 auto', display: 'block' }}>
      <div style={{ maxWidth: CONTAINER_MAX_WIDTH, margin: '0 auto' }}>
        {/* 顶部：标题 + 输入框 */}
        <Card
          className="glass-card"
          style={{ marginBottom: 16 }}
          styles={{ body: { padding: 24 } }}>
          <div style={{ marginBottom: 16 }}>{headerNode}</div>
          <Title level={5} style={{ marginTop: 0, marginBottom: 4 }}>
            提出你的问题
          </Title>
          <Text type="secondary" style={{ fontSize: 13 }}>
            用自然语言描述一次决策分析需求，系统将自动路由到对应 Agent（公司 / 行业 / 报告）。
          </Text>
          <div style={{ marginTop: 12 }}>
            <SearchInput
              value={inputValue}
              loading={isLoading}
              onChange={(v) => dispatch(setInput(v))}
              onSubmit={handleSubmit}
            />
          </div>
        </Card>

        {/* 下方：左历史 / 右结果 */}
        <Row gutter={[16, 16]}>
          <Col xs={24} md={9} lg={8}>
            <Card
              className="glass-card"
              title={
                <Space>
                  <span>历史分析</span>
                  {history.length > 0 && (
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      ({history.length})
                    </Text>
                  )}
                </Space>
              }
              extra={
                history.length > 0 ? (
                  <Button
                    size="small"
                    type="text"
                    icon={<ClearOutlined />}
                    onClick={() => dispatch(clearHistory())}>
                    清空
                  </Button>
                ) : null
              }
              styles={{ body: { padding: 12, maxHeight: 640, overflowY: 'auto' } }}>
              <HistoryList items={history} onPick={handlePick} />
            </Card>
          </Col>
          <Col xs={24} md={15} lg={16}>
            <Card
              className="glass-card"
              title={
                <Space>
                  <span>分析结果</span>
                  {currentResult && (
                    <Text type="secondary" style={{ fontSize: 12 }}>
                      {currentResult.analysisType}
                    </Text>
                  )}
                </Space>
              }
              styles={{ body: { padding: 24, minHeight: 320 } }}>
              <AnalysisResultView data={currentResult} loading={isLoading} error={error} />
            </Card>
          </Col>
        </Row>
      </div>
    </Spin>
  )
}

export default SearchPage
