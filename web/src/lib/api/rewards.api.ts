import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  BulkUploadVoucherRequest,
  CreateRewardRequest,
  PageResponseRewardResponse,
  RewardResponse,
  RewardsListParams,
  UpdateRewardRequest,
  VoucherPoolStatsResponse,
} from '../../types/rewards'

const BASE = '/api/v1/admin/rewards'

export async function listRewards(
  params: RewardsListParams = {},
): Promise<PageResponseRewardResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponseRewardResponse>>(BASE, {
    params: {
      page: params.page ?? 0,
      size: params.size ?? 20,
      ...(params.activeOnly !== undefined ? { activeOnly: params.activeOnly } : {}),
    },
  })
  return unwrapApiResponse(data)
}

export async function getReward(id: string): Promise<RewardResponse> {
  const { data } = await apiClient.get<ApiResponse<RewardResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function createReward(payload: CreateRewardRequest): Promise<RewardResponse> {
  const { data } = await apiClient.post<ApiResponse<RewardResponse>>(BASE, payload)
  return unwrapApiResponse(data)
}

export async function updateReward(
  id: string,
  payload: UpdateRewardRequest,
): Promise<RewardResponse> {
  const { data } = await apiClient.put<ApiResponse<RewardResponse>>(`${BASE}/${id}`, payload)
  return unwrapApiResponse(data)
}

export async function activateReward(id: string): Promise<RewardResponse> {
  const { data } = await apiClient.patch<ApiResponse<RewardResponse>>(`${BASE}/${id}/activate`)
  return unwrapApiResponse(data)
}

export async function deactivateReward(id: string): Promise<RewardResponse> {
  const { data } = await apiClient.patch<ApiResponse<RewardResponse>>(`${BASE}/${id}/deactivate`)
  return unwrapApiResponse(data)
}

export async function getRewardVoucherStats(id: string): Promise<VoucherPoolStatsResponse> {
  const { data } = await apiClient.get<ApiResponse<VoucherPoolStatsResponse>>(
    `${BASE}/${id}/vouchers/stats`,
  )
  return unwrapApiResponse(data)
}

export async function bulkUploadRewardVouchers(
  id: string,
  payload: BulkUploadVoucherRequest,
): Promise<VoucherPoolStatsResponse> {
  const { data } = await apiClient.post<ApiResponse<VoucherPoolStatsResponse>>(
    `${BASE}/${id}/vouchers/bulk-upload`,
    payload,
  )
  return unwrapApiResponse(data)
}
