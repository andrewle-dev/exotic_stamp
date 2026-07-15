import type { ActiveStateFilter } from '../../components/filters'
import type { RewardType } from '../../types/rewards'

export type RewardActiveFilter = ActiveStateFilter
export type RewardTypeFilter = RewardType | 'ALL'

/**
 * Rewards advanced filters.
 * activeOnly maps to the server when ACTIVE_ONLY; milestone / partner / type / search are client-side
 * on the current API page (list endpoint only supports activeOnly + pagination).
 */
export interface RewardFilters {
  active: RewardActiveFilter
  rewardType: RewardTypeFilter
  milestoneId: string
  partnerId: string
}

export const EMPTY_REWARD_FILTERS: RewardFilters = {
  active: 'ALL',
  rewardType: 'ALL',
  milestoneId: '',
  partnerId: '',
}

export const REWARD_TYPE_LABELS: Record<Exclude<RewardTypeFilter, 'ALL'>, string> = {
  VOUCHER: 'Voucher',
  DIGITAL_STICKER: 'Digital sticker',
  BONUS_STAMP: 'Bonus stamp',
}
