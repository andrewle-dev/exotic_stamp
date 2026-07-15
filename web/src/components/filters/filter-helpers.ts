import type { ActiveFilterItem } from './ActiveFilterChips'
import type { FilterAccent } from './filter-themes'

/** Truncate long filter values for compact tags. */
export function truncateFilterValue(value: string, max = 40): string {
  const trimmed = value.trim()
  if (trimmed.length <= max) {
    return trimmed
  }
  return `${trimmed.slice(0, Math.max(1, max - 1))}…`
}

export function buildSearchFilterTag(options: {
  search: string
  onRemove: () => void
}): ActiveFilterItem | null {
  if (!options.search) {
    return null
  }
  return {
    id: 'search',
    label: `Search: ${truncateFilterValue(options.search)}`,
    accent: 'search',
    onRemove: options.onRemove,
  }
}

export function buildLabeledFilterTag(options: {
  id: string
  label: string
  value: string
  accent?: FilterAccent
  onRemove: () => void
}): ActiveFilterItem {
  return {
    id: options.id,
    label: `${options.label}: ${truncateFilterValue(options.value)}`,
    accent: options.accent,
    onRemove: options.onRemove,
  }
}

export function collectFilterTags(
  items: Array<ActiveFilterItem | null | undefined | false>,
): ActiveFilterItem[] {
  return items.filter((item): item is ActiveFilterItem => Boolean(item))
}

/** Count applied advanced filters (exclude search) for the Filter button badge. */
export function countAppliedAdvancedFilters(
  predicates: Array<boolean | null | undefined>,
): number {
  return predicates.filter(Boolean).length
}
