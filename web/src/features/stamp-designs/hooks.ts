import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createStampDesign,
  deleteStampDesign,
  getStampDesign,
  listStampDesigns,
  reorderStampDesigns,
  updateStampDesign,
} from '../../lib/api/stamp-designs.api'
import { analyticsKeys } from '../../lib/query/keys/analytics'
import { stampDesignKeys } from '../../lib/query/keys/stamp-designs'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  CreateStampDesignRequest,
  StampDesignResponse,
  StampDesignsListParams,
  UpdateStampDesignRequest,
} from '../../types/stamp-designs'
import type { ReorderStampDesignsRequest } from '../../types/reorder'

export function useStampDesigns(params: StampDesignsListParams, options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: stampDesignKeys.list(params),
    queryFn: () => listStampDesigns(params),
    enabled: options?.enabled ?? true,
  })
}

export function useStampDesign(id: string | undefined) {
  return useQuery({
    queryKey: stampDesignKeys.detail(id ?? ''),
    queryFn: () => getStampDesign(id!),
    enabled: Boolean(id),
  })
}

function cacheStampDesignDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  design: StampDesignResponse,
) {
  queryClient.setQueryData(stampDesignKeys.detail(design.id), design)
}

export function useCreateStampDesign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateStampDesignRequest) => createStampDesign(payload),
    onSuccess: async (design) => {
      cacheStampDesignDetail(queryClient, design)
      await invalidateKeys(queryClient, [
        stampDesignKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useUpdateStampDesign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateStampDesignRequest }) =>
      updateStampDesign(id, body),
    onSuccess: async (design) => {
      cacheStampDesignDetail(queryClient, design)
      await invalidateKeys(queryClient, [
        stampDesignKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useDeleteStampDesign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteStampDesign(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: stampDesignKeys.detail(id) })
      await invalidateKeys(queryClient, [
        stampDesignKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useReorderStampDesigns() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: ReorderStampDesignsRequest) => reorderStampDesigns(body),
    onSuccess: async () => {
      await invalidateKeys(queryClient, [stampDesignKeys.lists()])
    },
  })
}
