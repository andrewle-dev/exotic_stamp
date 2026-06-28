import type { StationsListParams } from '../../../types/stations'

export const stationKeys = {
  all: ['stations'] as const,
  lists: () => [...stationKeys.all, 'list'] as const,
  list: (filters: StationsListParams) => [...stationKeys.lists(), filters] as const,
  details: () => [...stationKeys.all, 'detail'] as const,
  detail: (id: string) => [...stationKeys.details(), id] as const,
  stats: () => [...stationKeys.all, 'stats'] as const,
}
