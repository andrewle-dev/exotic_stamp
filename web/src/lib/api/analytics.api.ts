import { apiClient } from './client'
import type { ApiResponse } from '../../types/api'
import type {
  CollectionAdminStatsView,
  StationStatsResponse,
} from '../../types/analytics'

const COLLECTION_STATS_BASE = '/api/v1/admin/collections/stats'
const STATION_STATS_BASE = '/api/v1/admin/metro/stations/stats'

const EMPTY_COLLECTION_STATS: CollectionAdminStatsView = {
  totalStampsCollected: 0,
  stampsPerCampaign: [],
}

function unwrapOrFallback<T>(response: ApiResponse<T>, fallback: T): T {
  if (!response.success || response.data === null || response.data === undefined) {
    return fallback
  }

  return response.data
}

export async function getCollectionStats(): Promise<CollectionAdminStatsView> {
  const { data } = await apiClient.get<ApiResponse<CollectionAdminStatsView>>(COLLECTION_STATS_BASE)
  return unwrapOrFallback(data, EMPTY_COLLECTION_STATS)
}

export async function getStationStats(): Promise<StationStatsResponse[]> {
  const { data } = await apiClient.get<ApiResponse<StationStatsResponse[]>>(STATION_STATS_BASE)
  return unwrapOrFallback(data, [])
}
