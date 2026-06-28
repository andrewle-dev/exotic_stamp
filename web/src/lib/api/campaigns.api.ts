import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  AssignCampaignStationRequest,
  CampaignResponse,
  CampaignStationResponse,
  CampaignsListParams,
  CreateCampaignRequest,
  PageResponseCampaignResponse,
  UpdateCampaignRequest,
} from '../../types/campaigns'

const BASE = '/api/v1/admin/campaigns'

export async function listCampaigns(
  params: CampaignsListParams = {},
): Promise<PageResponseCampaignResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponseCampaignResponse>>(BASE, {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
    },
  })
  return unwrapApiResponse(data)
}

export async function getCampaign(id: string): Promise<CampaignResponse> {
  const { data } = await apiClient.get<ApiResponse<CampaignResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function createCampaign(body: CreateCampaignRequest): Promise<CampaignResponse> {
  const { data } = await apiClient.post<ApiResponse<CampaignResponse>>(BASE, body)
  return unwrapApiResponse(data)
}

export async function updateCampaign(
  id: string,
  body: UpdateCampaignRequest,
): Promise<CampaignResponse> {
  const { data } = await apiClient.patch<ApiResponse<CampaignResponse>>(`${BASE}/${id}`, body)
  return unwrapApiResponse(data)
}

export async function deleteCampaign(id: string): Promise<void> {
  await apiClient.delete(`${BASE}/${id}`)
}

export async function listCampaignStations(
  campaignId: string,
): Promise<CampaignStationResponse[]> {
  const { data } = await apiClient.get<ApiResponse<CampaignStationResponse[]>>(
    `${BASE}/${campaignId}/stations`,
  )
  return unwrapApiResponse(data)
}

export async function assignCampaignStation(
  campaignId: string,
  body: AssignCampaignStationRequest,
): Promise<void> {
  await apiClient.post(`${BASE}/${campaignId}/stations`, body)
}

export async function removeCampaignStation(
  campaignId: string,
  stationId: string,
): Promise<void> {
  await apiClient.delete(`${BASE}/${campaignId}/stations/${stationId}`)
}
