import React from 'react'
import ReactDOM from 'react-dom/client'
import { Provider } from 'react-redux'
import { ConfigProvider, App as AntdApp } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import 'dayjs/locale/zh-cn'
import dayjs from 'dayjs'

import App from './App'
import { store } from './stores'
import './index.css'

dayjs.locale('zh-cn')

// 应用入口：挂载 React 根节点，注入 Redux Provider、AntD ConfigProvider 与国际化
ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Provider store={store}>
      <ConfigProvider
        locale={zhCN}
        theme={{
          token: {
            colorPrimary: '#6366f1',
            colorInfo: '#6366f1',
            colorLink: '#6366f1',
            borderRadius: 10,
            fontSize: 14,
            colorText: '#0f172a',
            colorTextSecondary: '#475569',
            colorBgContainer: 'rgba(255,255,255,0.65)',
            colorBgElevated: '#ffffff',
            colorBorder: 'rgba(99,102,241,0.15)',
            colorBorderSecondary: 'rgba(99,102,241,0.08)',
            boxShadow: '0 8px 32px rgba(99,102,241,0.10)',
          },
          components: {
            Layout: {
              headerBg: 'rgba(255,255,255,0.55)',
              siderBg: 'rgba(255,255,255,0.55)',
              bodyBg: 'transparent',
              triggerBg: 'transparent',
            },
            Menu: {
              itemBg: 'transparent',
              subMenuItemBg: 'transparent',
              itemSelectedBg: 'rgba(99,102,241,0.12)',
              itemSelectedColor: '#6366f1',
              itemHoverBg: 'rgba(99,102,241,0.06)',
              itemHoverColor: '#4f46e5',
              itemColor: '#475569',
              itemBorderRadius: 10,
            },
          },
        }}>
        <AntdApp>
          <App />
        </AntdApp>
      </ConfigProvider>
    </Provider>
  </React.StrictMode>,
)
