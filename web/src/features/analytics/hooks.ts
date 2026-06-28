import { useQuery } from '@tanstack/react-query'
import { getCollectionStats, getStationStats } from '../../lib/api/analytics.api'
import { analyticsKeys } from '../../lib/query/keys/analytics'

export function useCollectionStats() {
  return useQuery({
    queryKey: analyticsKeys.collectionStats(),
    queryFn: () => getCollectionStats(),
  })
}

export function useStationStats() {
  return useQuery({
    queryKey: analyticsKeys.stationStats(),
    queryFn: () => getStationStats(),
  })
}
