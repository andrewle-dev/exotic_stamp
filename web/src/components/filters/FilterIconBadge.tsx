import type { LucideIcon } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { FILTER_THEMES, type FilterAccent } from './filter-themes'

interface FilterIconBadgeProps {
  accent?: FilterAccent
  icon?: LucideIcon
  className?: string
}

export function FilterIconBadge({
  accent = 'default',
  icon,
  className,
}: FilterIconBadgeProps) {
  const theme = FILTER_THEMES[accent]
  const Icon = icon ?? theme.icon

  return (
    <span
      className={cn(
        'inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-lg',
        theme.badge,
        className,
      )}
      aria-hidden
    >
      <Icon className="h-3.5 w-3.5" strokeWidth={2} />
    </span>
  )
}
