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
            colorPrimary: '#1677ff',
            borderRadius: 6,
          },
        }}>
        <AntdApp>
          <App />
        </AntdApp>
      </ConfigProvider>
    </Provider>
  </React.StrictMode>,
)
