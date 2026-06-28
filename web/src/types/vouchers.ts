import type { PageResponse } from './common'

export type VoucherStatus =
  | 'AVAILABLE'
  | 'ASSIGNED'
  | 'REDEEMED'
  | 'EXPIRED'
  | 'DISABLED'
  | (string & {})

export interface VoucherPoolResponse {
  id: string
  milestoneId: string
  code: string
  status: VoucherStatus
  assignedUserId?: string
  assignedUserRewardId?: string
  assignedAt?: string
  expiresAt?: string
  createdAt?: string
}

export interface ImportVouchersRequest {
  milestoneId: string
  codes: string[]
  expiresAt?: string
}

export type PageResponseVoucherPoolResponse = PageResponse<VoucherPoolResponse>

export interface VouchersListParams {
  milestoneId?: string
  status?: string
  page?: number
  size?: number
}

/** Map of result key -> count returned by voucher import endpoint. */
export type MapStringInteger = Record<string, number>
