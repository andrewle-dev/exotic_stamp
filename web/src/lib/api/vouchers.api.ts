import { apiClient } from './client'
import { unwrapApiResponse } from './response'
import type { ApiResponse } from '../../types/api'
import type {
  ImportVouchersRequest,
  MapStringInteger,
  PageResponseVoucherPoolResponse,
  VoucherPoolResponse,
  VouchersListParams,
} from '../../types/vouchers'

const BASE = '/api/v1/admin/rewards/vouchers'

export async function listVouchers(
  params: VouchersListParams = {},
): Promise<PageResponseVoucherPoolResponse> {
  const { data } = await apiClient.get<ApiResponse<PageResponseVoucherPoolResponse>>(BASE, {
    params: {
      milestoneId: params.milestoneId,
      status: params.status,
      page: params.page ?? 0,
      size: params.size ?? 20,
    },
  })
  return unwrapApiResponse(data)
}

export async function getVoucher(id: string): Promise<VoucherPoolResponse> {
  const { data } = await apiClient.get<ApiResponse<VoucherPoolResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function disableVoucher(id: string): Promise<VoucherPoolResponse> {
  const { data } = await apiClient.patch<ApiResponse<VoucherPoolResponse>>(`${BASE}/${id}`)
  return unwrapApiResponse(data)
}

export async function importVouchers(payload: ImportVouchersRequest): Promise<MapStringInteger> {
  const { data } = await apiClient.post<ApiResponse<MapStringInteger>>(`${BASE}/import`, payload)
  return unwrapApiResponse(data)
}
