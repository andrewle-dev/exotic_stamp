import { Bell, LogOut } from 'lucide-react'
import { useAuth } from '../../features/auth/hooks'
import { Badge } from '../ui/Badge'
import { Button } from '../ui/Button'

interface TopbarProps {
  title: string
}

export function Topbar({ title }: TopbarProps) {
  const { user, profile, logout } = useAuth()
  const displayName =
    profile?.firstname && profile?.lastname
      ? `${profile.firstname} ${profile.lastname}`
      : user?.username ?? 'Admin'

  const initials = displayName
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  return (
    <header className="flex h-[var(--topbar-height)] items-center justify-between border-b border-border bg-card px-6">
      <div className="flex items-center gap-3">
        <h1 className="text-lg font-semibold text-foreground">{title}</h1>
        {/* <Badge status="DRAFT" label={import.meta.env.VITE_APP_ENV ?? 'STAGING'} />
        <span className="hidden items-center gap-1 rounded-full border border-emerald-200 bg-emerald-50 px-2 py-0.5 text-xs text-emerald-700 md:inline-flex">
          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
          API Healthy
        </span> */}
      </div>

      <div className="flex items-center gap-3">
        <button
          type="button"
          className="relative rounded-md p-2 text-muted-foreground hover:bg-secondary"
          aria-label="Notifications"
        >
          <Bell className="h-4 w-4" />
          <span className="absolute right-1 top-1 flex h-4 w-4 items-center justify-center rounded-full bg-destructive text-[10px] font-semibold text-destructive-foreground">
            0
          </span>
        </button>

        <div className="hidden items-center gap-2 md:flex">
          <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">
            {initials}
          </div>
          <div className="text-right">
            <p className="text-sm font-medium text-foreground">{displayName}</p>
            <p className="text-xs text-muted-foreground">
              {user?.roles?.[0]?.replace('ROLE_', '') ?? 'Admin'}
            </p>
          </div>
        </div>

        <Button variant="ghost" size="sm" onClick={() => void logout()} aria-label="Sign out">
          <LogOut className="h-4 w-4" />
        </Button>
      </div>
    </header>
  )
}
