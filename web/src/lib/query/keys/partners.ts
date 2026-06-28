import type { PartnersListParams } from '../../../types/partners'

export const partnerKeys = {
  all: ['partners'] as const,
  lists: () => [...partnerKeys.all, 'list'] as const,
  list: (params: PartnersListParams) => [...partnerKeys.lists(), params] as const,
  details: () => [...partnerKeys.all, 'detail'] as const,
  detail: (id: string) => [...partnerKeys.details(), id] as const,
}
