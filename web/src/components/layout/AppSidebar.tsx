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

function CaretTriangle({ direction }: { direction: 'left' | 'right' }) {
  return (
    <svg
      viewBox="0 0 10 12"
      width="10"
      height="12"
      aria-hidden="true"
      className="shrink-0 fill-current"
    >
      {direction === 'left' ? (
        <polygon points="8,1 2,6 8,11" />
      ) : (
        <polygon points="2,1 8,6 2,11" />
      )}
    </svg>
  )
}

interface AppSidebarProps {
  collapsed: boolean
  onToggle: () => void
}

export function AppSidebar({ collapsed, onToggle }: AppSidebarProps) {
  return (
    <aside
      className={cn(
        'flex h-full shrink-0 flex-col overflow-hidden border-r border-border bg-card',
        'transition-[width] duration-300 ease-in-out motion-reduce:transition-none',
        collapsed ? 'w-20' : 'w-72',
      )}
    >
      <div
        className={cn(
          'relative flex h-[var(--topbar-height)] min-h-[var(--topbar-height)] items-center border-b border-border',
          collapsed ? 'justify-center px-2' : 'gap-3 px-4',
        )}
      >
        <div
          className={cn(
            'flex shrink-0 items-center justify-center rounded-2xl bg-white shadow-sm',
            collapsed ? 'h-12 w-12' : 'h-14 w-14',
          )}
        >
          <img
            src="/assets/logo/ExoticStamp_logo2.png"
            alt="Exotic Stamp"
            className={cn(
              'rounded-2xl object-contain',
              collapsed ? 'h-10 w-10' : 'h-12 w-12',
            )}
          />
        </div>

        <div
          className={cn(
            'min-w-0 overflow-hidden transition-all duration-300 ease-in-out motion-reduce:transition-none',
            collapsed
              ? 'pointer-events-none absolute h-0 w-0 -translate-x-2 opacity-0'
              : 'relative max-w-[180px] flex-1 translate-x-0 opacity-100',
          )}
        >
          <p className="truncate text-base font-semibold text-foreground">Exotic Stamp</p>
        </div>
      </div>

      <nav className="sidebar-scroll min-h-0 flex-1 space-y-1 overflow-y-auto overflow-x-hidden px-3 py-3">
        {NAV_ITEMS.map((item) => {
          const Icon = iconMap[item.icon as keyof typeof iconMap]
          return (
            <NavLink
              key={item.path}
              to={item.path}
              end={item.path === '/'}
              title={collapsed ? item.label : undefined}
              className={({ isActive }) =>
                cn(
                  'flex h-11 items-center rounded-md text-sm font-medium transition-colors',
                  collapsed ? 'justify-center px-0' : 'gap-3 px-3',
                  isActive
                    ? 'bg-secondary text-primary'
                    : 'text-muted-foreground hover:bg-secondary hover:text-foreground',
                )
              }
            >
              <Icon className="h-4 w-4 shrink-0" />
              <span
                className={cn(
                  'overflow-hidden whitespace-nowrap transition-all duration-300 ease-in-out motion-reduce:transition-none',
                  collapsed
                    ? 'pointer-events-none max-w-0 -translate-x-2 opacity-0'
                    : 'max-w-[180px] translate-x-0 opacity-100',
                )}
              >
                {item.label}
              </span>
            </NavLink>
          )
        })}
      </nav>

      <div
        className={cn(
          'flex shrink-0 border-t border-border p-3',
          collapsed ? 'justify-center' : 'justify-end',
        )}
      >
        <button
          type="button"
          onClick={onToggle}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          aria-expanded={!collapsed}
          className={cn(
            'inline-flex h-8 items-center justify-center rounded-lg border border-slate-200',
            'bg-slate-50 text-primary shadow-none',
            'transition-all duration-200 ease-out motion-reduce:transition-none',
            'hover:border-slate-300 hover:bg-slate-100',
            'active:bg-slate-100/80',
            'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1',
            collapsed ? 'w-10' : 'w-full max-w-full',
          )}
        >
          <CaretTriangle direction={collapsed ? 'right' : 'left'} />
        </button>
      </div>
    </aside>
  )
}
