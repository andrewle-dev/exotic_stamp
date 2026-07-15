import { useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { AppSidebar } from './AppSidebar'
import { Topbar } from './Topbar'
import { NAV_ITEMS } from '../../lib/constants/routes'

const SIDEBAR_COLLAPSED_KEY = 'exotic-stamp-admin-sidebar-collapsed'

function getPageTitle(pathname: string): string {
  if (pathname.startsWith('/stations/')) {
    return 'Station Detail'
  }
  if (pathname.startsWith('/campaigns/')) {
    return 'Campaign Detail'
  }

  const match = NAV_ITEMS.find((item) =>
    item.path === '/'
      ? pathname === '/'
      : pathname === item.path || pathname.startsWith(`${item.path}/`),
  )

  return match?.label ?? 'Admin Console'
}

function readCollapsedPreference(): boolean {
  try {
    const stored = localStorage.getItem(SIDEBAR_COLLAPSED_KEY)
    if (stored === 'true') return true
    if (stored === 'false') return false
    return window.matchMedia('(max-width: 1023px)').matches
  } catch {
    return false
  }
}

function writeCollapsedPreference(collapsed: boolean): void {
  try {
    localStorage.setItem(SIDEBAR_COLLAPSED_KEY, String(collapsed))
  } catch {
    // Ignore storage failures (private mode, quota, etc.)
  }
}

export function AdminLayout() {
  const { pathname } = useLocation()
  const title = getPageTitle(pathname)
  const [collapsed, setCollapsed] = useState(readCollapsedPreference)

  const handleToggle = () => {
    setCollapsed((prev) => {
      const next = !prev
      writeCollapsedPreference(next)
      return next
    })
  }

  return (
    <div className="flex h-dvh min-h-0 overflow-hidden bg-background">
      <AppSidebar collapsed={collapsed} onToggle={handleToggle} />
      <div className="flex min-h-0 min-w-0 flex-1 flex-col overflow-hidden">
        <Topbar title={title} />
        <main className="min-h-0 min-w-0 flex-1 overflow-y-auto overflow-x-hidden p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
