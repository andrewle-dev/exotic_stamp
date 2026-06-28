import type { StampDesignsListParams } from '../../../types/stamp-designs'

export const stampDesignKeys = {
  all: ['stamp-designs'] as const,
  lists: () => [...stampDesignKeys.all, 'list'] as const,
  list: (params: StampDesignsListParams) => [...stampDesignKeys.lists(), params] as const,
  details: () => [...stampDesignKeys.all, 'detail'] as const,
  detail: (id: string) => [...stampDesignKeys.details(), id] as const,
}
