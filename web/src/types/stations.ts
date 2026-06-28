import type { MetroStatus, PageResponse } from './common'

export type { MetroStatus, PageResponse }

export type ScanKeyStatus = 'ACTIVE' | 'INACTIVE'

export interface StationResponse {
  id: string
  lineId: string
  lineCode?: string
  lineName?: string
  code: string
  name: string
  displayName?: string
  description?: string
  address?: string
  imageUrl?: string
  stampPreviewUrl?: string
  latitude?: number
  longitude?: number
  zoneRadiusMeters?: number
  sortOrder?: number
  status: MetroStatus
  createdAt?: string
  updatedAt?: string
}

export interface StationDetailResponse extends StationResponse {
  nfcTagId?: string
  qrCodeValue?: string
  scanKeyStatus?: ScanKeyStatus
  lastQrRotatedAt?: string
  lastScanKeyUpdatedAt?: string
}

export interface CreateStationRequest {
  lineId: string
  code: string
  name: string
  displayName?: string
  description?: string
  address?: string
  sortOrder?: number
  latitude?: number
  longitude?: number
  zoneRadiusMeters?: number
  imageUrl?: string
  stampPreviewUrl?: string
  status?: MetroStatus
}

export interface UpdateStationRequest {
  code?: string
  name?: string
  displayName?: string
  description?: string
  address?: string
  sortOrder?: number
  latitude?: number
  longitude?: number
  zoneRadiusMeters?: number
  imageUrl?: string
  stampPreviewUrl?: string
  status?: MetroStatus
}

export interface UpdateScanKeysRequest {
  nfcTagId?: string
  qrCodeValue?: string
  scanKeyStatus?: ScanKeyStatus
}

export interface StationStatsResponse {
  stationId: string
  stationName: string
  lineName?: string
  collectorCount: number
}

export type PageResponseStationResponse = PageResponse<StationResponse>

export interface StationsListParams {
  lineId?: string
  status?: MetroStatus
  search?: string
  page?: number
  size?: number
  sort?: string
}
