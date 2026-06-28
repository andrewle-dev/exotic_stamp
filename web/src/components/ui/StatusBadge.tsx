import { cn } from '../../lib/utils/cn'

type StatusTone = 'success' | 'danger' | 'warning' | 'info' | 'neutral'

interface StatusMeta {
  tone: StatusTone
  label: string
}

const toneClasses: Record<StatusTone, string> = {
  success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
  danger: 'border-red-200 bg-accent text-accent-foreground',
  warning: 'border-amber-200 bg-amber-50 text-amber-700',
  info: 'border-blue-200 bg-secondary text-primary',
  neutral: 'border-border bg-secondary text-muted-foreground',
}

const toneDotClasses: Record<StatusTone, string> = {
  success: 'bg-emerald-500',
  danger: 'bg-destructive',
  warning: 'bg-amber-500',
  info: 'bg-primary',
  neutral: 'bg-muted-foreground',
}

/** Canonical status -> tone/label map covering all admin status groups. */
const STATUS_MAP: Record<string, StatusMeta> = {
  // Campaign type
  STANDARD: { tone: 'neutral', label: 'Standard' },
  SEASONAL: { tone: 'info', label: 'Seasonal' },
  EVENT: { tone: 'info', label: 'Event' },
  // Campaign schedule
  UPCOMING: { tone: 'info', label: 'Upcoming' },
  RUNNING: { tone: 'success', label: 'Running' },
  // Lifecycle
  ACTIVE: { tone: 'success', label: 'Active' },
  DRAFT: { tone: 'neutral', label: 'Draft' },
  INACTIVE: { tone: 'warning', label: 'Inactive' },
  ARCHIVED: { tone: 'neutral', label: 'Archived' },
  // Stamp rarity
  COMMON: { tone: 'neutral', label: 'Common' },
  RARE: { tone: 'info', label: 'Rare' },
  EPIC: { tone: 'warning', label: 'Epic' },
  LEGENDARY: { tone: 'danger', label: 'Legendary' },
  // Readiness
  READY: { tone: 'success', label: 'Ready' },
  NOT_READY: { tone: 'danger', label: 'Not Ready' },
  // Scan key
  CONFIGURED: { tone: 'success', label: 'Configured' },
  MISSING: { tone: 'danger', label: 'Missing' },
  // GPS
  GPS_OK: { tone: 'success', label: 'GPS OK' },
  GPS_MISSING: { tone: 'danger', label: 'GPS Missing' },
  // Voucher lifecycle
  AVAILABLE: { tone: 'success', label: 'Available' },
  ASSIGNED: { tone: 'info', label: 'Assigned' },
  CLAIMED: { tone: 'info', label: 'Claimed' },
  REDEEMED: { tone: 'info', label: 'Redeemed' },
  EXPIRED: { tone: 'warning', label: 'Expired' },
  DISABLED: { tone: 'neutral', label: 'Disabled' },
  // Stock
  OK: { tone: 'success', label: 'In Stock' },
  LOW_STOCK: { tone: 'warning', label: 'Low Stock' },
  OUT_OF_STOCK: { tone: 'danger', label: 'Out of Stock' },
  // Partner contract
  NO_CONTRACT: { tone: 'neutral', label: 'No Contract' },
  EXPIRING_SOON: { tone: 'warning', label: 'Expiring Soon' },
  ACTIVE_CONTRACT: { tone: 'success', label: 'Active Contract' },
  FUTURE_CONTRACT: { tone: 'info', label: 'Future Contract' },
  // Milestone reward type
  VOUCHER: { tone: 'info', label: 'Voucher' },
  DIGITAL_STICKER: { tone: 'warning', label: 'Digital Sticker' },
  BONUS_STAMP: { tone: 'success', label: 'Bonus Stamp' },
}

export type KnownStatus = keyof typeof STATUS_MAP

interface StatusBadgeProps {
  status: string
  /** Override the displayed label. */
  label?: string
  /** Show a leading status dot. Defaults to true. */
  dot?: boolean
  className?: string
}

function humanizeFallback(status: string): string {
  return status
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (char) => char.toUpperCase())
}

export function StatusBadge({ status, label, dot = true, className }: StatusBadgeProps) {
  const meta = STATUS_MAP[status]
  const tone: StatusTone = meta?.tone ?? 'neutral'
  const text = label ?? meta?.label ?? humanizeFallback(status)

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded border px-2 py-0.5 text-[11px] font-semibold uppercase tracking-wide whitespace-nowrap',
        toneClasses[tone],
        className,
      )}
    >
      {dot ? <span className={cn('h-1.5 w-1.5 rounded-full', toneDotClasses[tone])} /> : null}
      {text}
    </span>
  )
}
