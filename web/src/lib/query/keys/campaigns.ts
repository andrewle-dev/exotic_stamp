import type { CampaignsListParams } from '../../../types/campaigns'

export const campaignKeys = {
  all: ['campaigns'] as const,
  lists: () => [...campaignKeys.all, 'list'] as const,
  list: (params: CampaignsListParams) => [...campaignKeys.lists(), params] as const,
  details: () => [...campaignKeys.all, 'detail'] as const,
  detail: (id: string) => [...campaignKeys.details(), id] as const,
  stations: (id: string) => [...campaignKeys.all, 'stations', id] as const,
}
