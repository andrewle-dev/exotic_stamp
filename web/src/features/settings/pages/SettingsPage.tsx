import { useEffect, useMemo, useState } from 'react'
import { AlertTriangle, LogOut } from 'lucide-react'
import { useAuth } from '../../auth/hooks'
import { Card, CardContent, CardHeader, CardTitle } from '../../../components/ui/Card'
import { Button } from '../../../components/ui/Button'
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog'
import { normalizeRoleName } from '../../../lib/auth/permissions'
import { API_BASE_URL } from '../../../lib/api/client'
import { cn } from '../../../lib/utils/cn'

const APP_VERSION = (import.meta.env.VITE_APP_VERSION as string | undefined) ?? 'v1.0.0-beta'
const BUILD_DATE = (import.meta.env.VITE_BUILD_DATE as string | undefined) ?? '2024-06-25'
const CONFIGURED_ENV = (
  (import.meta.env.VITE_APP_ENV as string | undefined) ?? 'STAGING'
).toUpperCase()

const SESSION_STORAGE_KEY = 'es_session_started_at'

function getApiHost(): string {
  try {
    return new URL(API_BASE_URL).host
  } catch {
    return API_BASE_URL
  }
}

function toTitleCase(value: string): string {
  return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase()
}

function formatRole(role: string): string {
  return normalizeRoleName(role).split('_').map(toTitleCase).join(' ')
}

function formatClock(date: Date): string {
  return new Intl.DateTimeFormat(undefined, {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}

function formatSessionStart(date: Date): string {
  const now = new Date()
  const isToday =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()

  if (isToday) {
    return `Today at ${formatClock(date)}`
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date)
}

function getSessionStartedAt(): number {
  const stored = window.sessionStorage.getItem(SESSION_STORAGE_KEY)
  const parsed = stored ? Number(stored) : NaN
  if (!Number.isNaN(parsed) && parsed > 0) {
    return parsed
  }
  const now = Date.now()
  window.sessionStorage.setItem(SESSION_STORAGE_KEY, String(now))
  return now
}

interface InfoRowProps {
  label: string
  children: React.ReactNode
}

function InfoRow({ label, children }: InfoRowProps) {
  return (
    <div className="flex items-center justify-between gap-4 py-2 text-sm">
      <span className="text-muted-foreground">{label}</span>
      <span className="text-right font-medium text-foreground">{children}</span>
    </div>
  )
}

function MonoValue({ children }: { children: React.ReactNode }) {
  return (
    <span className="rounded bg-secondary px-2 py-0.5 font-mono text-xs text-foreground">
      {children}
    </span>
  )
}

export function SettingsPage() {
  const { user, profile, logout } = useAuth()

  const apiHost = useMemo(getApiHost, [])
  const [environment, setEnvironment] = useState(CONFIGURED_ENV)
  const [pendingEnvironment, setPendingEnvironment] = useState<string | null>(null)
  const [signingOut, setSigningOut] = useState(false)

  const [sessionStartedAt] = useState(getSessionStartedAt)
  const [lastActivityAt, setLastActivityAt] = useState(() => Date.now())

  useEffect(() => {
    const markActivity = () => setLastActivityAt(Date.now())
    const interval = window.setInterval(markActivity, 60_000)
    window.addEventListener('focus', markActivity)
    document.addEventListener('visibilitychange', markActivity)
    return () => {
      window.clearInterval(interval)
      window.removeEventListener('focus', markActivity)
      document.removeEventListener('visibilitychange', markActivity)
    }
  }, [])

  const displayName =
    profile?.firstname && profile?.lastname
      ? `${profile.firstname} ${profile.lastname}`
      : profile?.username ?? user?.username ?? 'Admin'

  const email = profile?.email ?? user?.email ?? '—'

  const initials = displayName
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  const primaryRoleLabel = user?.roles?.length ? formatRole(user.roles[0]) : 'Admin'

  const environmentOptions = useMemo(
    () => [
      { value: 'STAGING', label: `Staging (${apiHost})` },
      { value: 'PRODUCTION', label: 'Production (api.exoticstamp.vn)' },
    ],
    [apiHost],
  )

  const handleEnvironmentChange = (value: string) => {
    if (value === environment) {
      return
    }
    if (value === 'PRODUCTION') {
      setPendingEnvironment(value)
      return
    }
    setEnvironment(value)
  }

  const handleSignOut = async () => {
    setSigningOut(true)
    try {
      await logout()
    } finally {
      setSigningOut(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Admin Profile</CardTitle>
        </CardHeader>
        <CardContent className="flex items-center gap-4">
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-primary text-base font-semibold text-primary-foreground">
            {initials}
          </div>
          <div className="min-w-0 space-y-1">
            <p className="truncate font-semibold text-foreground">{displayName}</p>
            <p className="truncate text-sm text-muted-foreground">{email}</p>
            <span className="inline-flex items-center gap-1.5 rounded-full border border-emerald-200 bg-emerald-50 px-2 py-0.5 text-xs font-medium text-emerald-700">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
              {primaryRoleLabel}
            </span>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>API Environment</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex items-center gap-2 text-sm">
            <span className="text-muted-foreground">Current:</span>
            <span
              className={cn(
                'inline-flex items-center rounded border px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide',
                environment === 'PRODUCTION'
                  ? 'border-red-200 bg-red-50 text-red-700'
                  : 'border-amber-200 bg-amber-50 text-amber-700',
              )}
            >
              {environment}
            </span>
          </div>

          <select
            aria-label="API environment"
            value={environment}
            onChange={(event) => handleEnvironmentChange(event.target.value)}
            className="w-full rounded-md border border-border bg-input-background px-3 py-2 text-sm text-foreground outline-none transition-colors focus:border-primary focus:ring-2 focus:ring-ring/20"
          >
            {environmentOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>

          <p className="flex items-start gap-1.5 text-xs text-amber-600">
            <AlertTriangle className="mt-0.5 h-3.5 w-3.5 shrink-0" />
            Switching to Production affects all live users. A confirmation is required.
          </p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Session</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          <div className="divide-y divide-border">
            <InfoRow label="Signed in as">{email}</InfoRow>
            <InfoRow label="Session started">{formatSessionStart(new Date(sessionStartedAt))}</InfoRow>
            <InfoRow label="Last activity">{formatClock(new Date(lastActivityAt))}</InfoRow>
          </div>
          <div className="pt-2">
            <Button variant="danger" size="md" onClick={() => void handleSignOut()} disabled={signingOut}>
              <LogOut className="h-4 w-4" />
              {signingOut ? 'Signing out…' : 'Sign Out'}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>About</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="divide-y divide-border">
            <InfoRow label="Application">Exotic Stamp Admin Console</InfoRow>
            <InfoRow label="Version">
              <MonoValue>{APP_VERSION}</MonoValue>
            </InfoRow>
            <InfoRow label="Build date">
              <MonoValue>{BUILD_DATE}</MonoValue>
            </InfoRow>
            <InfoRow label="API">
              <MonoValue>{apiHost}</MonoValue>
            </InfoRow>
          </div>
        </CardContent>
      </Card>

      <ConfirmDialog
        open={pendingEnvironment !== null}
        variant="danger"
        title="Switch to Production?"
        description="Switching to Production affects all live users. Make sure you understand the impact before continuing."
        confirmLabel="Switch to Production"
        cancelLabel="Cancel"
        onConfirm={() => {
          if (pendingEnvironment) {
            setEnvironment(pendingEnvironment)
          }
          setPendingEnvironment(null)
        }}
        onCancel={() => setPendingEnvironment(null)}
      />
    </div>
  )
}
