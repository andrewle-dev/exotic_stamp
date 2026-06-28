import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  CreatePartnerRequest,
  PageResponsePartnerResponse,
  PartnerResponse,
  PartnersListParams,
  UpdatePartnerRequest,
} from '../../types/partners'

const BASE = '/api/v1/admin/partners'

export async function listPartners(
  params: PartnersListParams = {},
): Promise<PageResponsePartnerResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponsePartnerResponse>>(BASE, {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      ...(params.activeOnly !== undefined ? { activeOnly: params.activeOnly } : {}),
    },
  })
  return unwrapApiResponse(data)
}

export async function getPartner(id: string): Promise<PartnerResponse> {
  const { data } = await apiClient.get<ApiResponse<PartnerResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function createPartner(payload: CreatePartnerRequest): Promise<PartnerResponse> {
  const { data } = await apiClient.post<ApiResponse<PartnerResponse>>(BASE, payload)
  return unwrapApiResponse(data)
}

export async function updatePartner(
  id: string,
  payload: UpdatePartnerRequest,
): Promise<PartnerResponse> {
  const { data } = await apiClient.put<ApiResponse<PartnerResponse>>(`${BASE}/${id}`, payload)
  return unwrapApiResponse(data)
}

export async function activatePartner(id: string): Promise<PartnerResponse> {
  const { data } = await apiClient.patch<ApiResponse<PartnerResponse>>(`${BASE}/${id}/activate`)
  return unwrapApiResponse(data)
}

export async function deactivatePartner(id: string): Promise<PartnerResponse> {
  const { data } = await apiClient.patch<ApiResponse<PartnerResponse>>(`${BASE}/${id}/deactivate`)
  return unwrapApiResponse(data)
}
