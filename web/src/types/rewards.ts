import type { PageResponse } from './common'

export type RewardType = 'VOUCHER' | 'DIGITAL_STICKER' | 'BONUS_STAMP'

export interface RewardResponse {
  id: string
  milestoneId: string
  partnerId?: string
  rewardType: RewardType | string
  name: string
  description?: string
  valueAmount?: number
  expiryDays?: number
  totalStock?: number
  issuedCount?: number
  active?: boolean
}

export interface CreateRewardRequest {
  milestoneId: string
  partnerId?: string
  rewardType: RewardType
  name: string
  description?: string
  valueAmount?: number
  expiryDays?: number
  totalStock?: number
}

export interface UpdateRewardRequest {
  milestoneId?: string
  partnerId?: string
  rewardType?: RewardType
  name?: string
  description?: string
  valueAmount?: number
  expiryDays?: number
  totalStock?: number
}

export type PageResponseRewardResponse = PageResponse<RewardResponse>

export interface RewardsListParams {
  activeOnly?: boolean
  page?: number
  size?: number
}

export interface VoucherPoolStatsResponse {
  availableCount?: number
  redeemedCount?: number
}

export interface BulkUploadVoucherRequest {
  codes: string[]
}
