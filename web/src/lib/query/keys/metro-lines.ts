import type { MetroLinesListParams } from '../../../types/metro-lines'

export const metroLineKeys = {
  all: ['metro-lines'] as const,
  lists: () => [...metroLineKeys.all, 'list'] as const,
  list: (filters: MetroLinesListParams) => [...metroLineKeys.lists(), filters] as const,
  details: () => [...metroLineKeys.all, 'detail'] as const,
  detail: (id: string) => [...metroLineKeys.details(), id] as const,
}
