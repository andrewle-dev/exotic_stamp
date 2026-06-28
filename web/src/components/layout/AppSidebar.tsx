import { NavLink } from 'react-router-dom'
import {
  BarChart2,
  Gift,
  Handshake,
  LayoutDashboard,
  MapPin,
  Megaphone,
  Settings,
  ShieldCheck,
  Stamp,
  Train,
  Trophy,
} from 'lucide-react'
import { NAV_ITEMS } from '../../lib/constants/routes'
import { cn } from '../../lib/utils/cn'

const iconMap = {
  LayoutDashboard,
  Train,
  MapPin,
  Megaphone,
  Stamp,
  Handshake,
  Trophy,
  Gift,
  BarChart2,
  ShieldCheck,
  Settings,
} as const

interface AppSidebarProps {
  collapsed?: boolean
}

export function AppSidebar({ collapsed = false }: AppSidebarProps) {
  return (
    <aside
      className={cn(
        'flex h-full flex-col border-r border-border bg-card',
        collapsed ? 'w-16' : 'w-[var(--sidebar-width)]',
      )}
    >
      <div className="flex items-center gap-3 border-b border-border px-4 py-4">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground">
          <Train className="h-5 w-5" />
        </div>
        {!collapsed ? (
          <div>
            <p className="text-sm font-semibold text-foreground">Exotic Stamp</p>
            <p className="text-[11px] uppercase tracking-wide text-muted-foreground">Admin Console</p>
          </div>
        ) : null}
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto p-3">
        {NAV_ITEMS.map((item) => {
          const Icon = iconMap[item.icon as keyof typeof iconMap]
          return (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-secondary text-primary'
                    : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
                )
              }
            >
              <Icon className="h-4 w-4 shrink-0" />
              {!collapsed ? <span className="truncate">{item.label}</span> : null}
            </NavLink>
          )
        })}
      </nav>

      {!collapsed ? (
        <div className="border-t border-border px-4 py-3 text-[11px] text-muted-foreground">
          v1.0.0-beta · {import.meta.env.VITE_APP_ENV ?? 'STAGING'}
        </div>
      ) : null}
    </aside>
  )
}
