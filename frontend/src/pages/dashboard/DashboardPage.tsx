import { Row, Col, Card, Statistic } from 'antd'
import { BankOutlined, AppstoreOutlined, FileTextOutlined, RobotOutlined } from '@ant-design/icons'
import ReactECharts from 'echarts-for-react'

/** 总览页面：核心 KPI 卡片 + ECharts 图表占位 */
const DashboardPage = () => {
  // ECharts 占位：未来展示报告生成趋势
  const chartOption = {
    title: { text: '近 30 天报告生成趋势', left: 'center' },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 50, bottom: 40 },
    xAxis: {
      type: 'category',
      data: Array.from({ length: 30 }, (_, i) => `${i + 1} 日`),
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '报告数',
        type: 'line',
        smooth: true,
        areaStyle: {},
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50 + 10)),
      },
    ],
  }

  return (
    <div>
      <Row gutter={16}>
        <Col span={6}>
          <Card>
            <Statistic title="公司总数" value={132} prefix={<BankOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="行业数" value={24} prefix={<AppstoreOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="报告数" value={86} prefix={<FileTextOutlined />} />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic title="Agent 任务" value={12} prefix={<RobotOutlined />} />
          </Card>
        </Col>
      </Row>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col span={24}>
          <Card title="数据趋势">
            <ReactECharts option={chartOption} style={{ height: 360 }} />
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default DashboardPage
