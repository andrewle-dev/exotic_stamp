import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  CreateMilestoneRequest,
  MilestoneResponse,
  MilestonesListParams,
  PageResponseMilestoneResponse,
  UpdateMilestoneRequest,
} from '../../types/milestones'
import type { ReorderMilestonesRequest, ReorderResponse } from '../../types/reorder'

const BASE = '/api/v1/admin/rewards/milestones'

export async function listMilestones(
  params: MilestonesListParams = {},
): Promise<PageResponseMilestoneResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponseMilestoneResponse>>(BASE, {
    params: {
      campaignId: params.campaignId,
      status: params.status,
      page: params.page ?? 0,
      size: params.size ?? 20,
    },
  })
  return unwrapApiResponse(data)
}

export async function getMilestone(id: string): Promise<MilestoneResponse> {
  const { data } = await apiClient.get<ApiResponse<MilestoneResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function createMilestone(payload: CreateMilestoneRequest): Promise<MilestoneResponse> {
  const { data } = await apiClient.post<ApiResponse<MilestoneResponse>>(BASE, payload)
  return unwrapApiResponse(data)
}

export async function updateMilestone(
  id: string,
  payload: UpdateMilestoneRequest,
): Promise<MilestoneResponse> {
  const { data } = await apiClient.patch<ApiResponse<MilestoneResponse>>(`${BASE}/${id}`, payload)
  return unwrapApiResponse(data)
}

export async function deleteMilestone(id: string): Promise<void> {
  await apiClient.delete(`${BASE}/${id}`)
}

export async function reorderMilestones(body: ReorderMilestonesRequest): Promise<ReorderResponse> {
  const { data } = await apiClient.patch<ApiResponse<ReorderResponse>>(`${BASE}/reorder`, body)
  return unwrapApiResponse(data)
}
