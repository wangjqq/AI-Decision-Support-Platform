import { RouterProvider } from 'react-router-dom'
import { router } from './router'

// 应用根组件：直接挂载路由，所有页面级布局由路由表控制
const App = () => {
  return <RouterProvider router={router} />
}

export default App
