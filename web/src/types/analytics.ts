import type { ApiResponse } from './api'

export interface CampaignStampCountView {
  campaignId: string
  stampCount: number
}

export interface CollectionAdminStatsView {
  totalStampsCollected: number
  stampsPerCampaign: CampaignStampCountView[]
}

export interface StationStatsResponse {
  stationId: string
  stationName: string
  lineName?: string
  collectorCount: number
}

export type ApiResponseCollectionAdminStatsView = ApiResponse<CollectionAdminStatsView>

export type ApiResponseListStationStatsResponse = ApiResponse<StationStatsResponse[]>

export type StockStatus = 'OK' | 'LOW_STOCK' | 'OUT_OF_STOCK' | 'UNKNOWN'

export interface OperationalWarning {
  severity: 'warning' | 'danger'
  category: string
  message: string
}
