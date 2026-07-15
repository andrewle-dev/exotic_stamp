import type { MilestoneRewardType, MilestoneStatus } from '../../types/milestones'

export type MilestoneStatusFilter = MilestoneStatus | 'ALL'
export type MilestoneRewardTypeFilter = MilestoneRewardType | 'ALL'

export interface MilestoneFilters {
  campaignId: string
  status: MilestoneStatusFilter
  rewardType: MilestoneRewardTypeFilter
}

export const EMPTY_MILESTONE_FILTERS: MilestoneFilters = {
  campaignId: '',
  status: 'ALL',
  rewardType: 'ALL',
}

export const MILESTONE_REWARD_TYPE_LABELS: Record<
  Exclude<MilestoneRewardTypeFilter, 'ALL'>,
  string
> = {
  VOUCHER: 'Voucher',
  DIGITAL_STICKER: 'Digital sticker',
  BONUS_STAMP: 'Bonus stamp',
}
