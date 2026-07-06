import { Row, Col, Card, Statistic, Space, Progress, Tag } from 'antd'
import {
  BankOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  RobotOutlined,
  RiseOutlined,
  ThunderboltFilled,
  ClockCircleOutlined,
  CheckCircleFilled,
  ArrowUpOutlined,
} from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'

/** 总览页面：玻璃拟态风格 · KPI + 多图表 + AI 任务流 */
const DashboardPage = () => {
  /* ---------- 报告生成趋势 ---------- */
  const trendOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: 30, right: 16, top: 30, bottom: 28 },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 30 }, (_, i) => `${i + 1}`),
      axisLine: { lineStyle: { color: 'rgba(99,102,241,0.15)' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(99,102,241,0.06)' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
    },
    series: [
      {
        name: '报告数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3, color: '#6366f1' },
        itemStyle: { color: '#6366f1' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(99,102,241,0.35)' },
              { offset: 1, color: 'rgba(99,102,241,0)' },
            ],
          },
        },
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 10)),
      },
    ],
  }

  /* ---------- 行业分布饼图 ---------- */
  const pieOption = {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#475569', fontSize: 11 } },
    series: [
      {
        name: '行业分布',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: [
          { value: 1048, name: '制造业', itemStyle: { color: '#6366f1' } },
          { value: 735, name: '金融业', itemStyle: { color: '#06b6d4' } },
          { value: 580, name: '信息技术', itemStyle: { color: '#8b5cf6' } },
          { value: 484, name: '零售业', itemStyle: { color: '#22c55e' } },
          { value: 300, name: '其他', itemStyle: { color: '#f59e0b' } },
        ],
      },
    ],
  }

  /* ---------- 柱状图：报告类型 ---------- */
  const barOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: 30, right: 16, top: 20, bottom: 28 },
    xAxis: {
      type: 'category',
      data: ['行业研究', '公司画像', '竞品分析', '财务洞察', '政策解读'],
      axisLine: { lineStyle: { color: 'rgba(99,102,241,0.15)' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: 'rgba(99,102,241,0.06)' } },
      axisLabel: { color: '#94a3b8', fontSize: 10 },
    },
    series: [
      {
        data: [42, 28, 19, 15, 8],
        type: 'bar',
        barWidth: 28,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: {
            type: 'linear',
            x: 0, y: 0, x2: 0, y2: 1,
            colorStops: [
              { offset: 0, color: '#6366f1' },
              { offset: 1, color: '#06b6d4' },
            ],
          },
        },
      },
    ],
  }

  /* ---------- 雷达图：AI 能力评估 ---------- */
  const radarOption = {
    tooltip: {},
    radar: {
      indicator: [
        { name: '数据采集', max: 100 },
        { name: '知识检索', max: 100 },
        { name: '推理分析', max: 100 },
        { name: '报告生成', max: 100 },
        { name: '多模态', max: 100 },
      ],
      radius: '65%',
      splitLine: { lineStyle: { color: 'rgba(99,102,241,0.12)' } },
      splitArea: { areaStyle: { color: ['rgba(99,102,241,0.02)', 'rgba(99,102,241,0.04)'] } },
      axisLine: { lineStyle: { color: 'rgba(99,102,241,0.15)' } },
      name: { textStyle: { color: '#475569', fontSize: 11 } },
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: [88, 92, 76, 85, 70],
            name: '当前能力',
            areaStyle: { color: 'rgba(99,102,241,0.25)' },
            lineStyle: { color: '#6366f1', width: 2 },
            itemStyle: { color: '#6366f1' },
          },
        ],
      },
    ],
  }

  return (
    <div>
      {/* 欢迎 Banner */}
      <div
        className="glass-panel"
        style={{
          padding: '20px 24px',
          marginBottom: 20,
          background: 'linear-gradient(135deg, rgba(99,102,241,0.12), rgba(6,182,212,0.08))',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          overflow: 'hidden',
          position: 'relative',
        }}>
        <div>
          <div style={{ fontSize: 12, color: '#6366f1', fontWeight: 600, letterSpacing: 1 }}>
            <ThunderboltFilled style={{ marginRight: 6 }} />
            AI 决策控制台
          </div>
          <div style={{ fontSize: 22, fontWeight: 700, marginTop: 6, color: '#0f172a' }}>
            欢迎回来，<span className="gradient-text">管理员</span>
          </div>
          <div style={{ fontSize: 13, color: '#64748b', marginTop: 4 }}>
            今日有 <b style={{ color: '#6366f1' }}>12</b> 个 Agent 任务正在运行，已生成 <b style={{ color: '#06b6d4' }}>28</b> 份报告
          </div>
        </div>
        <div
          style={{
            width: 88,
            height: 88,
            borderRadius: 24,
            background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 8px 28px rgba(99,102,241,0.35)',
          }}>
          <RiseOutlined style={{ fontSize: 38, color: '#fff' }} />
        </div>
      </div>

      {/* KPI 卡片 */}
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Card className="glass-card" styles={{ body: { padding: 20 } }}>
            <Space size={14} align="start">
              <div className="icon-badge"><BankOutlined /></div>
              <div>
                <div style={{ fontSize: 12, color: '#94a3b8' }}>公司总数</div>
                <div className="gradient-number" style={{ fontSize: 28, lineHeight: 1.2, marginTop: 4 }}>
                  132
                </div>
                <div style={{ fontSize: 11, color: '#22c55e', marginTop: 2 }}>
                  <ArrowUpOutlined /> 12.5% 较上月
                </div>
              </div>
            </Space>
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="glass-card" styles={{ body: { padding: 20 } }}>
            <Space size={14} align="start">
              <div className="icon-badge cyan"><AppstoreOutlined /></div>
              <div>
                <div style={{ fontSize: 12, color: '#94a3b8' }}>行业数</div>
                <div className="gradient-number" style={{ fontSize: 28, lineHeight: 1.2, marginTop: 4 }}>
                  24
                </div>
                <div style={{ fontSize: 11, color: '#22c55e', marginTop: 2 }}>
                  <ArrowUpOutlined /> 4.2% 较上月
                </div>
              </div>
            </Space>
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="glass-card" styles={{ body: { padding: 20 } }}>
            <Space size={14} align="start">
              <div className="icon-badge violet"><FileTextOutlined /></div>
              <div>
                <div style={{ fontSize: 12, color: '#94a3b8' }}>报告数</div>
                <div className="gradient-number" style={{ fontSize: 28, lineHeight: 1.2, marginTop: 4 }}>
                  86
                </div>
                <div style={{ fontSize: 11, color: '#22c55e', marginTop: 2 }}>
                  <ArrowUpOutlined /> 28 今日新增
                </div>
              </div>
            </Space>
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="glass-card" styles={{ body: { padding: 20 } }}>
            <Space size={14} align="start">
              <div className="icon-badge green"><RobotOutlined /></div>
              <div>
                <div style={{ fontSize: 12, color: '#94a3b8' }}>Agent 任务</div>
                <div className="gradient-number" style={{ fontSize: 28, lineHeight: 1.2, marginTop: 4 }}>
                  12
                </div>
                <div style={{ fontSize: 11, color: '#f59e0b', marginTop: 2 }}>
                  <ClockCircleOutlined /> 8 任务处理中
                </div>
              </div>
            </Space>
          </Card>
        </Col>
      </Row>

      {/* 主图表区 */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={16}>
          <Card
            className="glass-card"
            title={
              <Space>
                <span className="gradient-text" style={{ fontWeight: 700 }}>报告生成趋势</span>
                <Tag color="processing" style={{ marginLeft: 4 }}>近 30 天</Tag>
              </Space>
            }
            extra={<span style={{ fontSize: 12, color: '#94a3b8' }}>数据更新于 10:23</span>}>
            <ReactECharts option={trendOption} style={{ height: 320 }} />
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card className="glass-card" title={<span className="gradient-text" style={{ fontWeight: 700 }}>行业分布</span>}>
            <ReactECharts option={pieOption} style={{ height: 320 }} />
          </Card>
        </Col>
      </Row>

      {/* 次图表区 */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card className="glass-card" title={<span className="gradient-text" style={{ fontWeight: 700 }}>报告类型分布</span>}>
            <ReactECharts option={barOption} style={{ height: 280 }} />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card className="glass-card" title={<span className="gradient-text" style={{ fontWeight: 700 }}>AI 能力雷达</span>}>
            <ReactECharts option={radarOption} style={{ height: 280 }} />
          </Card>
        </Col>
      </Row>

      {/* AI 任务流 + 系统指标 */}
      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={14}>
          <Card
            className="glass-card"
            title={
              <Space>
                <span className="gradient-text" style={{ fontWeight: 700 }}>AI Agent 任务流</span>
                <Tag icon={<ThunderboltFilled />} color="purple">实时</Tag>
              </Space>
            }>
            <div>
              {[
                { name: '行业研究报告 - 新能源', progress: 78, status: 'running', color: '#6366f1' },
                { name: '公司画像 - 比亚迪集团', progress: 100, status: 'success', color: '#22c55e' },
                { name: '竞品分析 - 智能手机赛道', progress: 45, status: 'running', color: '#06b6d4' },
                { name: '政策解读 - 金融监管新规', progress: 92, status: 'running', color: '#8b5cf6' },
                { name: '财务洞察 - 上市公司年报', progress: 30, status: 'pending', color: '#f59e0b' },
              ].map((task, i) => (
                <div
                  key={i}
                  style={{
                    padding: '12px 0',
                    borderBottom: i < 4 ? '1px solid rgba(99,102,241,0.08)' : 'none',
                  }}>
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: 6,
                    }}>
                    <Space>
                      {task.status === 'success' ? (
                        <CheckCircleFilled style={{ color: '#22c55e' }} />
                      ) : task.status === 'running' ? (
                        <ThunderboltFilled style={{ color: task.color }} />
                      ) : (
                        <ClockCircleOutlined style={{ color: '#f59e0b' }} />
                      )}
                      <span style={{ fontSize: 13, color: '#0f172a', fontWeight: 500 }}>{task.name}</span>
                    </Space>
                    <span style={{ fontSize: 12, color: '#94a3b8' }}>{task.progress}%</span>
                  </div>
                  <Progress
                    percent={task.progress}
                    showInfo={false}
                    strokeColor={{
                      '0%': task.color,
                      '100%': '#06b6d4',
                    }}
                    trailColor="rgba(99,102,241,0.08)"
                  />
                </div>
              ))}
            </div>
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card
            className="glass-card"
            title={<span className="gradient-text" style={{ fontWeight: 700 }}>系统运行指标</span>}>
            {[
              { label: 'CPU 使用率', value: 42, color: '#6366f1' },
              { label: '内存占用', value: 68, color: '#06b6d4' },
              { label: 'AI 模型响应', value: 85, color: '#8b5cf6' },
              { label: '知识库索引', value: 56, color: '#22c55e' },
            ].map((m, i) => (
              <div key={i} style={{ marginBottom: 16 }}>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    fontSize: 13,
                    marginBottom: 6,
                  }}>
                  <span style={{ color: '#475569' }}>{m.label}</span>
                  <span className="gradient-number">{m.value}%</span>
                </div>
                <Progress
                  percent={m.value}
                  showInfo={false}
                  strokeColor={m.color}
                  trailColor="rgba(99,102,241,0.08)"
                />
              </div>
            ))}
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default DashboardPage
