import { useMemo } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { Layout, Menu, theme, type MenuProps } from 'antd'
import {
  DashboardOutlined,
  BankOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons'

import { useAppDispatch, useAppSelector } from '../hooks/redux'
import { toggleCollapsed } from '../stores/slices/appSlice'

const { Header, Sider, Content } = Layout

interface MenuConfig {
  key: string
  icon: React.ReactNode
  label: string
}

const MENU_ITEMS: MenuConfig[] = [
  { key: '/dashboard', icon: <DashboardOutlined />, label: '总览' },
  { key: '/companies', icon: <BankOutlined />, label: '公司管理' },
  { key: '/industries', icon: <AppstoreOutlined />, label: '行业管理' },
  { key: '/reports', icon: <FileTextOutlined />, label: '报告管理' },
]

/** 应用整体布局：左侧菜单 + 顶部 Header + 内容区（嵌套路由通过 Outlet 渲染） */
const AppLayout = () => {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const collapsed = useAppSelector((s) => s.app.collapsed)

  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken()

  // 根据当前路径计算菜单选中项
  const selectedKey = useMemo(() => {
    const matched = MENU_ITEMS.find((m) => location.pathname.startsWith(m.key))
    return matched ? matched.key : '/dashboard'
  }, [location.pathname])

  const menuItems: MenuProps['items'] = MENU_ITEMS.map((m) => ({
    key: m.key,
    icon: m.icon,
    label: m.label,
  }))

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key)
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} theme="dark" width={220}>
        <div
          style={{
            height: 64,
            margin: 0,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: collapsed ? 14 : 16,
            fontWeight: 600,
            letterSpacing: 1,
            background: 'rgba(255,255,255,0.05)',
          }}>
          {collapsed ? 'AIDSP' : 'AI 决策支持平台'}
        </div>
        <Menu theme="dark" mode="inline" selectedKeys={[selectedKey]} items={menuItems} onClick={handleMenuClick} />
      </Sider>
      <Layout>
        <Header
          style={{
            padding: '0 16px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,21,41,.08)',
          }}>
          <div
            onClick={() => dispatch(toggleCollapsed())}
            style={{ cursor: 'pointer', fontSize: 18 }}
            aria-label="切换侧边栏">
            {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
          </div>
          <div style={{ color: 'rgba(0,0,0,0.65)' }}>欢迎使用 AI 决策支持平台</div>
        </Header>
        <Content
          style={{
            margin: 16,
            padding: 24,
            minHeight: 280,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
          }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default AppLayout
