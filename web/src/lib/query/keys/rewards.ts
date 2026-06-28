import type { RewardsListParams } from '../../../types/rewards'

export const rewardKeys = {
  all: ['rewards'] as const,
  lists: () => [...rewardKeys.all, 'list'] as const,
  list: (params: RewardsListParams) => [...rewardKeys.lists(), params] as const,
  details: () => [...rewardKeys.all, 'detail'] as const,
  detail: (id: string) => [...rewardKeys.details(), id] as const,
  voucherStats: (id: string) => [...rewardKeys.all, 'voucher-stats', id] as const,
}
