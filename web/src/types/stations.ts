import type { MetroStatus, PageResponse } from './common'

export type { MetroStatus, PageResponse }

export type ScanKeyStatus = 'ACTIVE' | 'INACTIVE'

export type StationScanKeyStatus =
  | 'DRAFT'
  | 'ACTIVE'
  | 'INACTIVE'
  | 'REVOKED'
  | 'LOST'
  | 'REPLACED'

export type StationScanType = 'NFC' | 'QR_STATIC' | 'QR_DYNAMIC_PLACEHOLDER'

export interface StationScanKeyResponse {
  id: string
  stationId: string
  scanType: StationScanType
  keyPrefix: string
  payloadScheme?: string
  label?: string
  placementNote?: string
  status: StationScanKeyStatus
  activatedAt?: string
  revokedAt?: string
  replacedById?: string
  lastSeenAt?: string
  lastInstallVerifiedAt?: string
  installedLatitude?: number
  installedLongitude?: number
  installedAccuracyMeters?: number
  installedDevicePlatform?: string
  installedAppVersion?: string
  installedBy?: string
  createdBy?: string
  createdAt?: string
  updatedAt?: string
}

export interface StationScanKeyCreatedResponse {
  id: string
  stationId: string
  scanType: StationScanType
  /** Shown only once at creation — never persist in frontend storage. */
  payloadToWrite: string
  keyPrefix: string
  status: StationScanKeyStatus
  label?: string
  placementNote?: string
}

export interface CreateStationScanKeyRequest {
  scanType: StationScanType
  label?: string
  placementNote?: string
}

export interface RevokeStationScanKeyRequest {
  reason?: string
}

export interface VerifyStationScanKeyInstallationRequest {
  payloadReadBack: string
  latitude?: number
  longitude?: number
  accuracyMeters?: number
  devicePlatform?: string
  appVersion?: string
}

export interface StationScanKeyVerifyResponse {
  verified: boolean
  id: string
  stationId: string
  lastInstallVerifiedAt?: string
}


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
