import { createBrowserRouter, Navigate } from 'react-router-dom'
import { AdminLayout } from '../components/layout/AdminLayout'
import { RequireAuth } from '../components/layout/RequireAuth'
import { LoginPage } from '../features/auth/pages/LoginPage'
import { DashboardPage } from '../features/dashboard/pages/DashboardPage'
import { MetroLinesPage } from '../features/metro-lines/pages/MetroLinesPage'
import { StationsPage } from '../features/stations/pages/StationsPage'
import { StationDetailPage } from '../features/stations/pages/StationDetailPage'
import { CampaignsPage } from '../features/campaigns/pages/CampaignsPage'
import { CampaignDetailPage } from '../features/campaigns/pages/CampaignDetailPage'
import { StampDesignsPage } from '../features/stamp-designs/pages/StampDesignsPage'
import { PartnersPage } from '../features/partners/pages/PartnersPage'
import { MilestonesPage } from '../features/milestones/pages/MilestonesPage'
import { RewardsPage } from '../features/rewards/pages/RewardsPage'
import { AnalyticsPage } from '../features/analytics/pages/AnalyticsPage'
import { RbacPage } from '../features/rbac/pages/RbacPage'
import { SettingsPage } from '../features/settings/pages/SettingsPage'
import { ROUTES } from '../lib/constants/routes'

export const router = createBrowserRouter([
  {
    path: ROUTES.login,
    element: <LoginPage />,
  },
  {
    path: '/',
    element: (
      <RequireAuth>
        <AdminLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <DashboardPage /> },
      { path: 'metro-lines', element: <MetroLinesPage /> },
      { path: 'stations', element: <StationsPage /> },
      { path: 'stations/:id', element: <StationDetailPage /> },
      { path: 'campaigns', element: <CampaignsPage /> },
      { path: 'campaigns/:id', element: <CampaignDetailPage /> },
      { path: 'stamp-designs', element: <StampDesignsPage /> },
      { path: 'partners', element: <PartnersPage /> },
      { path: 'milestones', element: <MilestonesPage /> },
      { path: 'rewards', element: <RewardsPage /> },
      { path: 'analytics', element: <AnalyticsPage /> },
      { path: 'rbac', element: <RbacPage /> },
      { path: 'settings', element: <SettingsPage /> },
    ],
  },
  {
    path: '*',
    element: <Navigate to={ROUTES.dashboard} replace />,
  },
])
