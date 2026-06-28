import type { PageResponse } from './common'

export type MilestoneRewardType = 'VOUCHER' | 'DIGITAL_STICKER' | 'BONUS_STAMP'

export type MilestoneStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED'

export interface MilestoneResponse {
  id: string
  campaignId: string
  code: string
  requiredStampCount: number
  name: string
  description?: string
  rewardType: MilestoneRewardType
  rewardTitle: string
  rewardDescription?: string
  rewardImageUrl?: string
  status?: MilestoneStatus
  sortOrder?: number
  deletedAt?: string
}

export interface CreateMilestoneRequest {
  campaignId: string
  code: string
  requiredStampCount: number
  name: string
  description?: string
  rewardType: MilestoneRewardType
  rewardTitle: string
  rewardDescription?: string
  rewardImageUrl?: string
  status?: MilestoneStatus
  sortOrder?: number
}

export interface UpdateMilestoneRequest {
  code?: string
  requiredStampCount?: number
  name?: string
  description?: string
  rewardType?: MilestoneRewardType
  rewardTitle?: string
  rewardDescription?: string
  rewardImageUrl?: string
  status?: MilestoneStatus
  sortOrder?: number
}

export type PageResponseMilestoneResponse = PageResponse<MilestoneResponse>

export interface MilestonesListParams {
  campaignId?: string
  status?: string
  page?: number
  size?: number
}
