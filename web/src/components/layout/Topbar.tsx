import { Bell, ChevronDown, LogOut, Settings as SettingsIcon } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../features/auth/hooks'
import { ROUTES } from '../../lib/constants/routes'

interface TopbarProps {
  title: string
}

export function Topbar({ title }: TopbarProps) {
  const { user, profile, logout } = useAuth()
  const navigate = useNavigate()
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const menuRef = useRef<HTMLDivElement>(null)

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

  useEffect(() => {
    if (!isMenuOpen) return

    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
    }
  }, [isMenuOpen])

  const handleSettings = () => {
    setIsMenuOpen(false)
    navigate(ROUTES.settings)
  }

  const handleLogout = () => {
    setIsMenuOpen(false)
    void logout()
  }

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

        <div className="relative" ref={menuRef}>
          <button
            type="button"
            onClick={() => setIsMenuOpen((prev) => !prev)}
            aria-expanded={isMenuOpen}
            aria-haspopup="menu"
            className="flex items-center gap-2 rounded-md px-2 py-1.5 text-left transition-colors hover:bg-secondary"
          >
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground">
              {initials}
            </div>
            <div className="hidden text-right md:block">
              <p className="text-sm font-medium text-foreground">{displayName}</p>
              <p className="text-xs text-muted-foreground">
                {user?.roles?.[0]?.replace('ROLE_', '') ?? 'Admin'}
              </p>
            </div>
            <ChevronDown
              className={`h-4 w-4 text-muted-foreground transition-transform ${isMenuOpen ? 'rotate-180' : ''}`}
            />
          </button>

          {isMenuOpen ? (
            <div className="absolute right-0 top-full z-50 mt-2 w-48 rounded-lg border border-border bg-card p-2 shadow-lg">
              <button
                type="button"
                onClick={handleSettings}
                className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm text-foreground transition-colors hover:bg-secondary"
              >
                <SettingsIcon className="h-4 w-4" />
                Settings
              </button>
              <button
                type="button"
                onClick={handleLogout}
                className="flex w-full items-center gap-2 rounded-md px-3 py-2 text-sm text-foreground transition-colors hover:bg-secondary"
              >
                <LogOut className="h-4 w-4" />
                Logout
              </button>
            </div>
          ) : null}
        </div>
      </div>
    </header>
  )
}
