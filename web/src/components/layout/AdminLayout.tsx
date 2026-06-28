import { Outlet, useLocation } from 'react-router-dom'
import { AppSidebar } from './AppSidebar'
import { Topbar } from './Topbar'
import { NAV_ITEMS } from '../../lib/constants/routes'

function getPageTitle(pathname: string): string {
  if (pathname.startsWith('/stations/')) {
    return 'Station Detail'
  }

  const match = NAV_ITEMS.find((item) =>
    item.path === '/'
      ? pathname === '/'
      : pathname === item.path || pathname.startsWith(`${item.path}/`),
  )

  return match?.label ?? 'Admin Console'
}

export function AdminLayout() {
  const { pathname } = useLocation()
  const title = getPageTitle(pathname)

  return (
    <div className="flex min-h-dvh bg-background">
      <AppSidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <Topbar title={title} />
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
