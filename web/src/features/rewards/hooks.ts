import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  activateReward,
  bulkUploadRewardVouchers,
  createReward,
  deactivateReward,
  getReward,
  getRewardVoucherStats,
  listRewards,
  updateReward,
} from '../../lib/api/rewards.api'
import { analyticsKeys } from '../../lib/query/keys/analytics'
import { rewardKeys } from '../../lib/query/keys/rewards'
import { voucherKeys } from '../../lib/query/keys/vouchers'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  BulkUploadVoucherRequest,
  CreateRewardRequest,
  RewardResponse,
  RewardsListParams,
  UpdateRewardRequest,
} from '../../types/rewards'

export function useRewards(params: RewardsListParams) {
  return useQuery({
    queryKey: rewardKeys.list(params),
    queryFn: () => listRewards(params),
  })
}

export function useReward(id: string | undefined) {
  return useQuery({
    queryKey: rewardKeys.detail(id ?? ''),
    queryFn: () => getReward(id!),
    enabled: Boolean(id),
  })
}

function cacheRewardDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  reward: RewardResponse,
) {
  queryClient.setQueryData(rewardKeys.detail(reward.id), reward)
}

export function useCreateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateRewardRequest) => createReward(payload),
    onSuccess: async (reward) => {
      cacheRewardDetail(queryClient, reward)
      await invalidateKeys(queryClient, [rewardKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}

export function useUpdateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateRewardRequest }) =>
      updateReward(id, body),
    onSuccess: async (reward) => {
      cacheRewardDetail(queryClient, reward)
      await invalidateKeys(queryClient, [rewardKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}

export function useActivateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => activateReward(id),
    onSuccess: async (reward) => {
      cacheRewardDetail(queryClient, reward)
      await invalidateKeys(queryClient, [rewardKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}

export function useDeactivateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deactivateReward(id),
    onSuccess: async (reward) => {
      cacheRewardDetail(queryClient, reward)
      await invalidateKeys(queryClient, [rewardKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}

export function useRewardVoucherStats(id: string | undefined) {
  return useQuery({
    queryKey: rewardKeys.voucherStats(id ?? ''),
    queryFn: () => getRewardVoucherStats(id!),
    enabled: Boolean(id),
  })
}

export function useBulkUploadRewardVouchers() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: BulkUploadVoucherRequest }) =>
      bulkUploadRewardVouchers(id, body),
    onSuccess: async (_data, variables) => {
      await invalidateKeys(queryClient, [
        voucherKeys.lists(),
        rewardKeys.voucherStats(variables.id),
        rewardKeys.detail(variables.id),
        rewardKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}
