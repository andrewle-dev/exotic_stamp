import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  CreateLineRequest,
  LineDetailResponse,
  LineResponse,
  MetroLinesListParams,
  PageResponseLineResponse,
  UpdateLineRequest,
} from '../../types/metro-lines'
import type { ReorderLinesRequest, ReorderResponse } from '../../types/reorder'

const BASE = '/api/v1/admin/metro/lines'

export async function listMetroLines(
  params: MetroLinesListParams = {},
): Promise<PageResponseLineResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponseLineResponse>>(BASE, {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      ...(params.status ? { status: params.status } : {}),
      ...(params.search ? { search: params.search } : {}),
      ...(params.sort ? { sort: params.sort } : {}),
    },
  })
  return unwrapApiResponse(data)
}

export async function getMetroLine(id: string): Promise<LineDetailResponse> {
  const { data } = await apiClient.get<ApiResponse<LineDetailResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function createMetroLine(body: CreateLineRequest): Promise<LineResponse> {
  const { data } = await apiClient.post<ApiResponse<LineResponse>>(BASE, body)
  return unwrapApiResponse(data)
}

export async function updateMetroLine(
  id: string,
  body: UpdateLineRequest,
): Promise<LineResponse> {
  const { data } = await apiClient.patch<ApiResponse<LineResponse>>(`${BASE}/${id}`, body)
  return unwrapApiResponse(data)
}

export async function deleteMetroLine(id: string): Promise<void> {
  await apiClient.delete(`${BASE}/${id}`)
}

export async function reorderMetroLines(body: ReorderLinesRequest): Promise<ReorderResponse> {
  const { data } = await apiClient.patch<ApiResponse<ReorderResponse>>(`${BASE}/reorder`, body)
  return unwrapApiResponse(data)
}
