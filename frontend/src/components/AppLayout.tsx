import { useMemo } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'
import { Layout, Menu, type MenuProps } from 'antd'
import {
  DashboardOutlined,
  BankOutlined,
  AppstoreOutlined,
  FileTextOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ThunderboltFilled,
  ThunderboltOutlined,
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
  { key: '/search', icon: <ThunderboltOutlined />, label: '智能分析' },
]

/** 应用整体布局：玻璃侧边栏 + 玻璃 Header + 玻璃内容区 */
const AppLayout = () => {
  const dispatch = useAppDispatch()
  const navigate = useNavigate()
  const location = useLocation()
  const collapsed = useAppSelector((s) => s.app.collapsed)

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
    <Layout style={{ minHeight: '100vh', background: 'transparent' }}>
      {/* 玻璃侧边栏 */}
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        width={220}
        collapsedWidth={72}
        style={{
          background: 'rgba(255,255,255,0.55)',
          backdropFilter: 'blur(20px) saturate(180%)',
          WebkitBackdropFilter: 'blur(20px) saturate(180%)',
          borderRight: '1px solid rgba(255,255,255,0.6)',
          boxShadow: '4px 0 24px rgba(99,102,241,0.06)',
          position: 'relative',
          zIndex: 10,
        }}>
        {/* Logo */}
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'flex-start',
            padding: collapsed ? 0 : '0 20px',
            gap: 10,
            borderBottom: '1px solid rgba(99,102,241,0.08)',
            background: 'linear-gradient(135deg, rgba(99,102,241,0.06), rgba(6,182,212,0.04))',
          }}>
          <div
            style={{
              width: 32,
              height: 32,
              borderRadius: 10,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
              boxShadow: '0 4px 14px rgba(99,102,241,0.35)',
              color: '#fff',
              fontSize: 16,
              flexShrink: 0,
            }}>
            <ThunderboltFilled />
          </div>
          {!collapsed && (
            <div style={{ lineHeight: 1.2 }}>
              <div
                style={{
                  fontSize: 15,
                  fontWeight: 700,
                  letterSpacing: 0.5,
                  background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
                  WebkitBackgroundClip: 'text',
                  backgroundClip: 'text',
                  WebkitTextFillColor: 'transparent',
                }}>
                AI 决策平台
              </div>
              <div style={{ fontSize: 10, color: '#94a3b8', marginTop: 2, letterSpacing: 1 }}>DECISION SUITE</div>
            </div>
          )}
        </div>

        <Menu
          mode="inline"
          selectedKeys={[selectedKey]}
          items={menuItems}
          onClick={handleMenuClick}
          style={{
            background: 'transparent',
            borderInlineEnd: 'none',
            padding: '12px 8px',
            fontWeight: 500,
          }}
        />
      </Sider>

      <Layout style={{ background: 'transparent' }}>
        {/* 玻璃 Header */}
        <Header
          style={{
            padding: '0 24px',
            background: 'rgba(255,255,255,0.55)',
            backdropFilter: 'blur(20px) saturate(180%)',
            WebkitBackdropFilter: 'blur(20px) saturate(180%)',
            borderBottom: '1px solid rgba(255,255,255,0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            position: 'sticky',
            top: 0,
            zIndex: 9,
          }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <div
              onClick={() => dispatch(toggleCollapsed())}
              style={{
                cursor: 'pointer',
                fontSize: 18,
                width: 36,
                height: 36,
                borderRadius: 10,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#6366f1',
                background: 'rgba(99,102,241,0.08)',
                transition: 'all 0.2s',
              }}
              aria-label="切换侧边栏">
              {collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            </div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
              <div style={{ fontSize: 14, fontWeight: 600, color: '#0f172a' }}>
                {MENU_ITEMS.find((m) => m.key === selectedKey)?.label ?? '总览'}
              </div>
              <div style={{ fontSize: 11, color: '#94a3b8' }}>AI Decision Support Platform</div>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <span className="status-dot online" />
            <span style={{ fontSize: 12, color: '#475569' }}>系统正常</span>
            <div
              style={{
                width: 1,
                height: 16,
                background: 'rgba(99,102,241,0.15)',
                margin: '0 4px',
              }}
            />
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                padding: '4px 12px 4px 4px',
                borderRadius: 999,
                background: 'rgba(99,102,241,0.08)',
                cursor: 'pointer',
              }}>
              <div
                style={{
                  width: 28,
                  height: 28,
                  borderRadius: '50%',
                  background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: '#fff',
                  fontSize: 12,
                  fontWeight: 600,
                }}>
                AI
              </div>
              <span style={{ fontSize: 13, color: '#0f172a', fontWeight: 500 }}>管理员</span>
            </div>
          </div>
        </Header>

        {/* 内容区 */}
        <Content
          style={{
            margin: '20px',
            padding: 24,
            minHeight: 280,
            background: 'rgba(255,255,255,0.45)',
            backdropFilter: 'blur(20px) saturate(180%)',
            WebkitBackdropFilter: 'blur(20px) saturate(180%)',
            border: '1px solid rgba(255,255,255,0.6)',
            boxShadow: '0 8px 32px rgba(99,102,241,0.08)',
            borderRadius: 20,
          }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default AppLayout
