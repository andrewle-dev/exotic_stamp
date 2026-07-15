import type { LucideIcon } from 'lucide-react'
import { X } from 'lucide-react'
import { cn } from '../../lib/utils/cn'
import { UNIFIED_FILTER_TAG_STYLE, type FilterAccent } from './filter-themes'

interface FilterTagProps {
  label: string
  /** Kept for API compatibility; ignored — tags always use the unified blue style. */
  accent?: FilterAccent
  /** Optional leading icon; omitted by default for a quieter tag row. */
  icon?: LucideIcon
  onRemove: () => void
  className?: string
}

/** Soft blue removable chip for an applied filter value. */
export function FilterTag({
  label,
  icon: Icon,
  onRemove,
  className,
}: FilterTagProps) {
  return (
    <span
      className={cn(
        'inline-flex h-7 max-w-full items-center gap-1.5 rounded-full border px-2.5 text-xs font-medium',
        UNIFIED_FILTER_TAG_STYLE,
        className,
      )}
    >
      {Icon ? <Icon className="h-3 w-3 shrink-0 opacity-80" aria-hidden /> : null}
      <span className="truncate">{label}</span>
      <button
        type="button"
        onClick={onRemove}
        className="ml-0.5 inline-flex h-4 w-4 shrink-0 items-center justify-center rounded-full text-primary/70 transition-colors hover:bg-primary/10 hover:text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/40"
        aria-label={`Remove filter ${label}`}
      >
        <X className="h-3 w-3" />
      </button>
    </span>
  )
}

/** @deprecated Prefer FilterTag — same component, legacy name. */
export const FilterChip = FilterTag
