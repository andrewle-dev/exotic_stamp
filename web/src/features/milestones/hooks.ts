import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createMilestone,
  deleteMilestone,
  getMilestone,
  listMilestones,
  reorderMilestones,
  updateMilestone,
} from '../../lib/api/milestones.api'
import { analyticsKeys } from '../../lib/query/keys/analytics'
import { milestoneKeys } from '../../lib/query/keys/milestones'
import { rewardKeys } from '../../lib/query/keys/rewards'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  CreateMilestoneRequest,
  MilestoneResponse,
  MilestonesListParams,
  UpdateMilestoneRequest,
} from '../../types/milestones'
import type { ReorderMilestonesRequest } from '../../types/reorder'

export function useMilestones(params: MilestonesListParams, options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: milestoneKeys.list(params),
    queryFn: () => listMilestones(params),
    enabled: options?.enabled ?? true,
  })
}

export function useMilestone(id: string | undefined) {
  return useQuery({
    queryKey: milestoneKeys.detail(id ?? ''),
    queryFn: () => getMilestone(id!),
    enabled: Boolean(id),
  })
}

function cacheMilestoneDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  milestone: MilestoneResponse,
) {
  queryClient.setQueryData(milestoneKeys.detail(milestone.id), milestone)
}

export function useCreateMilestone() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateMilestoneRequest) => createMilestone(payload),
    onSuccess: async (milestone) => {
      cacheMilestoneDetail(queryClient, milestone)
      await invalidateKeys(queryClient, [
        milestoneKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useUpdateMilestone() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateMilestoneRequest }) =>
      updateMilestone(id, body),
    onSuccess: async (milestone) => {
      cacheMilestoneDetail(queryClient, milestone)
      // Reward tables resolve milestone labels from milestone lists.
      await invalidateKeys(queryClient, [
        milestoneKeys.lists(),
        rewardKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useDeleteMilestone() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteMilestone(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: milestoneKeys.detail(id) })
      await invalidateKeys(queryClient, [
        milestoneKeys.lists(),
        rewardKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useReorderMilestones() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: ReorderMilestonesRequest) => reorderMilestones(body),
    onSuccess: async () => {
      await invalidateKeys(queryClient, [milestoneKeys.lists()])
    },
  })
}
