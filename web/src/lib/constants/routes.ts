export const ROUTES = {
  login: '/login',
  dashboard: '/',
  metroLines: '/metro-lines',
  stations: '/stations',
  stationDetail: (id: string) => `/stations/${id}` as const,
  campaigns: '/campaigns',
  campaignDetail: (id: string) => `/campaigns/${id}` as const,
  stampDesigns: '/stamp-designs',
  partners: '/partners',
  milestones: '/milestones',
  rewards: '/rewards',
  analytics: '/analytics',
  rbac: '/rbac',
  settings: '/settings',
} as const

export interface NavItem {
  label: string
  path: string
  icon: string
}

export const NAV_ITEMS = [
  { label: 'Dashboard', path: ROUTES.dashboard, icon: 'LayoutDashboard' },
  { label: 'Metro Lines', path: ROUTES.metroLines, icon: 'Train' },
  { label: 'Stations', path: ROUTES.stations, icon: 'MapPin' },
  { label: 'Campaigns', path: ROUTES.campaigns, icon: 'Megaphone' },
  { label: 'Stamp Designs', path: ROUTES.stampDesigns, icon: 'Stamp' },
  { label: 'Partners', path: ROUTES.partners, icon: 'Handshake' },
  { label: 'Milestones', path: ROUTES.milestones, icon: 'Trophy' },
  { label: 'Rewards & Vouchers', path: ROUTES.rewards, icon: 'Gift' },
  { label: 'Analytics', path: ROUTES.analytics, icon: 'BarChart2' },
  { label: 'RBAC', path: ROUTES.rbac, icon: 'ShieldCheck' },
] as const
