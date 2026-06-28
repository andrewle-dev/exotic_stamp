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
import { rewardKeys } from '../../lib/query/keys/rewards'
import { voucherKeys } from '../../lib/query/keys/vouchers'
import type {
  BulkUploadVoucherRequest,
  CreateRewardRequest,
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

export function useCreateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateRewardRequest) => createReward(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: rewardKeys.lists() })
    },
  })
}

export function useUpdateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateRewardRequest }) =>
      updateReward(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: rewardKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.detail(variables.id) })
    },
  })
}

export function useActivateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => activateReward(id),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: rewardKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.detail(id) })
    },
  })
}

export function useDeactivateReward() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deactivateReward(id),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: rewardKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.detail(id) })
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
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: voucherKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.voucherStats(variables.id) })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.detail(variables.id) })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.lists() })
    },
  })
}
