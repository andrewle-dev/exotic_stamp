import type { LucideIcon } from 'lucide-react'
import type { FilterAccent } from './filter-themes'
import { FilterTag } from './FilterChip'

export interface ActiveFilterItem {
  id: string
  label: string
  /** Kept for API compatibility; tags always render in the unified blue style. */
  accent?: FilterAccent
  icon?: LucideIcon
  onRemove: () => void
}

interface ActiveFilterTagsProps {
  filters: ActiveFilterItem[]
  onClearAll: () => void
  label?: string
}

/**
 * Removable applied-filter tags shown below search / advanced filters.
 * Only renders when at least one applied filter (including search) exists.
 * All tags use the unified blue design system.
 */
export function ActiveFilterTags({
  filters,
  onClearAll,
  label = 'Active filters',
}: ActiveFilterTagsProps) {
  if (filters.length === 0) {
    return null
  }

  return (
    <div className="flex flex-wrap items-center gap-2">
      <span className="text-xs font-medium text-muted-foreground">{label}</span>
      {filters.map((filter) => (
        <FilterTag
          key={filter.id}
          label={filter.label}
          icon={filter.icon}
          onRemove={filter.onRemove}
        />
      ))}
      <button
        type="button"
        onClick={onClearAll}
        className="rounded px-1.5 py-0.5 text-xs font-medium text-muted-foreground transition-colors hover:bg-destructive/10 hover:text-destructive focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/40"
      >
        Clear all
      </button>
    </div>
  )
}

/** @deprecated Prefer ActiveFilterTags — same component, legacy name. */
export const ActiveFilterChips = ActiveFilterTags
