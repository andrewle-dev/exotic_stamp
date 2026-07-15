import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  CreateStationRequest,
  PageResponseStationResponse,
  StationDetailResponse,
  StationStatsResponse,
  StationsListParams,
  UpdateScanKeysRequest,
  UpdateStationRequest,
} from '../../types/stations'
import type { ReorderResponse, ReorderStationsRequest } from '../../types/reorder'

const BASE = '/api/v1/admin/metro/stations'

export async function listStations(
  params: StationsListParams = {},
): Promise<PageResponseStationResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponseStationResponse>>(BASE, {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      ...(params.lineId ? { lineId: params.lineId } : {}),
      ...(params.status ? { status: params.status } : {}),
      ...(params.search ? { search: params.search } : {}),
      ...(params.sort ? { sort: params.sort } : {}),
    },
  })
  return unwrapApiResponse(data)
}

export async function getStationStats(): Promise<StationStatsResponse[]> {
  const { data } = await apiClient.get<ApiResponse<StationStatsResponse[]>>(`${BASE}/stats`)
  return unwrapApiResponse(data)
}

export async function getStation(id: string): Promise<StationDetailResponse> {
  const { data } = await apiClient.get<ApiResponse<StationDetailResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function createStation(body: CreateStationRequest): Promise<StationDetailResponse> {
  const { data } = await apiClient.post<ApiResponse<StationDetailResponse>>(BASE, body)
  return unwrapApiResponse(data)
}

export async function updateStation(
  id: string,
  body: UpdateStationRequest,
): Promise<StationDetailResponse> {
  const { data } = await apiClient.patch<ApiResponse<StationDetailResponse>>(
    `${BASE}/${id}`,
    body,
  )
  return unwrapApiResponse(data)
}

export async function deleteStation(id: string): Promise<void> {
  await apiClient.delete(`${BASE}/${id}`)
}

export async function reorderStations(body: ReorderStationsRequest): Promise<ReorderResponse> {
  const { data } = await apiClient.patch<ApiResponse<ReorderResponse>>(`${BASE}/reorder`, body)
  return unwrapApiResponse(data)
}

export async function updateStationScanKeys(
  id: string,
  body: UpdateScanKeysRequest,
): Promise<StationDetailResponse> {
  const { data } = await apiClient.patch<ApiResponse<StationDetailResponse>>(
    `${BASE}/${id}/scan-keys`,
    body,
  )
  return unwrapApiResponse(data)
}

export async function rotateStationQr(id: string): Promise<StationDetailResponse> {
  const { data } = await apiClient.post<ApiResponse<StationDetailResponse>>(
    `${BASE}/${id}/rotate-qr`,
  )
  return unwrapApiResponse(data)
}
