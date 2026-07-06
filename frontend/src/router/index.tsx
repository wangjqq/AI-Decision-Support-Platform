import { createBrowserRouter, Navigate } from 'react-router-dom';

import AppLayout from '../components/AppLayout';
import NotFound from '../components/NotFound';
import DashboardPage from '../pages/dashboard/DashboardPage';
import CompanyListPage from '../pages/company/CompanyListPage';
import CompanyDetailPage from '../pages/company/CompanyDetailPage';
import IndustryListPage from '../pages/industry/IndustryListPage';
import IndustryDetailPage from '../pages/industry/IndustryDetailPage';
import ReportListPage from '../pages/report/ReportListPage';
import ReportDetailPage from '../pages/report/ReportDetailPage';

/**
 * 路由表
 * - 根路径 / 跳转 /dashboard
 * - AppLayout 作为嵌套布局 wrapper，子路由通过 Outlet 渲染
 */
export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'companies', element: <CompanyListPage /> },
      { path: 'companies/:id', element: <CompanyDetailPage /> },
      { path: 'industries', element: <IndustryListPage /> },
      { path: 'industries/:id', element: <IndustryDetailPage /> },
      { path: 'reports', element: <ReportListPage /> },
      { path: 'reports/:id', element: <ReportDetailPage /> },
      { path: '*', element: <NotFound /> },
    ],
  },
]);

export default router;
