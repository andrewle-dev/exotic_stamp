import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { cn } from '../../lib/utils/cn'
import { FILTER_THEMES, type FilterAccent } from './filter-themes'
import { FilterIconBadge } from './FilterIconBadge'

interface FilterGroupProps {
  id: string
  label: string
  accent?: FilterAccent
  icon?: LucideIcon
  children: ReactNode
  className?: string
}

export function FilterGroup({
  id,
  label,
  accent = 'default',
  icon,
  children,
  className,
}: FilterGroupProps) {
  const theme = FILTER_THEMES[accent]

  return (
    <div className={cn('min-w-[140px] flex-1 space-y-2', className)}>
      <div className="flex items-center gap-2">
        <FilterIconBadge accent={accent} icon={icon} />
        <label htmlFor={id} className={cn('text-xs font-semibold', theme.label)}>
          {label}
        </label>
      </div>
      {children}
    </div>
  )
}
