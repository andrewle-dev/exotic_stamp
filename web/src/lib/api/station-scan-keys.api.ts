import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  CreateStationScanKeyRequest,
  RevokeStationScanKeyRequest,
  StationScanKeyCreatedResponse,
  StationScanKeyResponse,
  StationScanKeyVerifyResponse,
  VerifyStationScanKeyInstallationRequest,
} from '../../types/stations'

const STATIONS_BASE = '/api/v1/admin/metro/stations'
const SCAN_KEYS_BASE = '/api/v1/admin/metro/scan-keys'

export async function listStationScanKeys(stationId: string): Promise<StationScanKeyResponse[]> {
  const { data } = await apiClient.get<ApiResponse<StationScanKeyResponse[]>>(
    `${STATIONS_BASE}/${stationId}/scan-keys`,
  )
  return unwrapApiResponse(data)
}

export async function createStationScanKey(
  stationId: string,
  body: CreateStationScanKeyRequest,
): Promise<StationScanKeyCreatedResponse> {
  const { data } = await apiClient.post<ApiResponse<StationScanKeyCreatedResponse>>(
    `${STATIONS_BASE}/${stationId}/scan-keys`,
    body,
  )
  return unwrapApiResponse(data)
}

export async function activateStationScanKey(id: string): Promise<StationScanKeyResponse> {
  const { data } = await apiClient.patch<ApiResponse<StationScanKeyResponse>>(
    `${SCAN_KEYS_BASE}/${id}/activate`,
  )
  return unwrapApiResponse(data)
}

export async function revokeStationScanKey(
  id: string,
  body: RevokeStationScanKeyRequest = {},
): Promise<StationScanKeyResponse> {
  const { data } = await apiClient.patch<ApiResponse<StationScanKeyResponse>>(
    `${SCAN_KEYS_BASE}/${id}/revoke`,
    body,
  )
  return unwrapApiResponse(data)
}

export async function markLostStationScanKey(id: string): Promise<StationScanKeyResponse> {
  const { data } = await apiClient.patch<ApiResponse<StationScanKeyResponse>>(
    `${SCAN_KEYS_BASE}/${id}/mark-lost`,
  )
  return unwrapApiResponse(data)
}

export async function verifyStationScanKeyInstallation(
  id: string,
  body: VerifyStationScanKeyInstallationRequest,
): Promise<StationScanKeyVerifyResponse> {
  const { data } = await apiClient.post<ApiResponse<StationScanKeyVerifyResponse>>(
    `${SCAN_KEYS_BASE}/${id}/verify-installation`,
    body,
  )
  return unwrapApiResponse(data)
}
