import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  CreateStampDesignRequest,
  PageResponseStampDesignResponse,
  StampDesignResponse,
  StampDesignsListParams,
  UpdateStampDesignRequest,
} from '../../types/stamp-designs'
import type { ReorderResponse, ReorderStampDesignsRequest } from '../../types/reorder'

const BASE = '/api/v1/admin/stamp-designs'

export async function listStampDesigns(
  params: StampDesignsListParams = {},
): Promise<PageResponseStampDesignResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponseStampDesignResponse>>(BASE, {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      ...(params.campaignId ? { campaignId: params.campaignId } : {}),
    },
  })
  return unwrapApiResponse(data)
}

export async function getStampDesign(id: string): Promise<StampDesignResponse> {
  const { data } = await apiClient.get<ApiResponse<StampDesignResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function createStampDesign(
  payload: CreateStampDesignRequest,
): Promise<StampDesignResponse> {
  const { data } = await apiClient.post<ApiResponse<StampDesignResponse>>(BASE, payload)
  return unwrapApiResponse(data)
}

export async function updateStampDesign(
  id: string,
  payload: UpdateStampDesignRequest,
): Promise<StampDesignResponse> {
  const { data } = await apiClient.patch<ApiResponse<StampDesignResponse>>(`${BASE}/${id}`, payload)
  return unwrapApiResponse(data)
}

export async function deleteStampDesign(id: string): Promise<void> {
  await apiClient.delete(`${BASE}/${id}`)
}

export async function reorderStampDesigns(
  body: ReorderStampDesignsRequest,
): Promise<ReorderResponse> {
  const { data } = await apiClient.patch<ApiResponse<ReorderResponse>>(`${BASE}/reorder`, body)
  return unwrapApiResponse(data)
}
