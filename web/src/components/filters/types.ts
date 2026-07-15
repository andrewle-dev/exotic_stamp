/**
 * Base pagination + search shape for admin list filters.
 * Each page extends this conceptually with its own advanced filter fields.
 */
export type ListFiltersBase = {
  q?: string
  page: number
  limit: number
}

/**
 * Active / inactive tri-state used by Partners and Rewards lists.
 */
export type ActiveStateFilter = 'ALL' | 'ACTIVE_ONLY' | 'INACTIVE_ONLY'

export const ACTIVE_STATE_FILTER_LABELS: Record<
  Exclude<ActiveStateFilter, 'ALL'>,
  string
> = {
  ACTIVE_ONLY: 'Active only',
  INACTIVE_ONLY: 'Inactive only',
}
