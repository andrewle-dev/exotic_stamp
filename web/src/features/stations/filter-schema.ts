import type { MetroStatus } from '../../types/common'

export type StationStatusFilter = MetroStatus | 'ALL'

export interface StationFilters {
  lineId: string
  status: StationStatusFilter
}

export const EMPTY_STATION_FILTERS: StationFilters = {
  lineId: '',
  status: 'ALL',
}
