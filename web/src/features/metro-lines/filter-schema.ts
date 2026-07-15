import type { MetroStatus } from '../../types/metro-lines'

export type MetroLineStatusFilter = MetroStatus | 'ALL'

export interface MetroLineFilters {
  status: MetroLineStatusFilter
}

export const EMPTY_LINE_FILTERS: MetroLineFilters = {
  status: 'ALL',
}
