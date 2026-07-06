import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    host: '0.0.0.0',
    port: 5173,
    open: false,
    proxy: {
      // 后端 API 基础路径代理：将 /api 请求转发到后端 8013
      '/api': {
        target: 'http://localhost:8013',
        changeOrigin: true,
      },
    },
  },
})
