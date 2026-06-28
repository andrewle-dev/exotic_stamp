import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createMilestone,
  deleteMilestone,
  getMilestone,
  listMilestones,
  updateMilestone,
} from '../../lib/api/milestones.api'
import { milestoneKeys } from '../../lib/query/keys/milestones'
import type {
  CreateMilestoneRequest,
  MilestonesListParams,
  UpdateMilestoneRequest,
} from '../../types/milestones'

export function useMilestones(params: MilestonesListParams) {
  return useQuery({
    queryKey: milestoneKeys.list(params),
    queryFn: () => listMilestones(params),
  })
}

export function useMilestone(id: string | undefined) {
  return useQuery({
    queryKey: milestoneKeys.detail(id ?? ''),
    queryFn: () => getMilestone(id!),
    enabled: Boolean(id),
  })
}

export function useCreateMilestone() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateMilestoneRequest) => createMilestone(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: milestoneKeys.lists() })
    },
  })
}

export function useUpdateMilestone() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateMilestoneRequest }) =>
      updateMilestone(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: milestoneKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: milestoneKeys.detail(variables.id) })
    },
  })
}

export function useDeleteMilestone() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteMilestone(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: milestoneKeys.lists() })
    },
  })
}
