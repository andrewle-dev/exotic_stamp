import type { MilestonesListParams } from '../../../types/milestones'

export const milestoneKeys = {
  all: ['milestones'] as const,
  lists: () => [...milestoneKeys.all, 'list'] as const,
  list: (params: MilestonesListParams) => [...milestoneKeys.lists(), params] as const,
  details: () => [...milestoneKeys.all, 'detail'] as const,
  detail: (id: string) => [...milestoneKeys.details(), id] as const,
}
