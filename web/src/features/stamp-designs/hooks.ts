import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createStampDesign,
  deleteStampDesign,
  getStampDesign,
  listStampDesigns,
  updateStampDesign,
} from '../../lib/api/stamp-designs.api'
import { stampDesignKeys } from '../../lib/query/keys/stamp-designs'
import type {
  CreateStampDesignRequest,
  StampDesignsListParams,
  UpdateStampDesignRequest,
} from '../../types/stamp-designs'

export function useStampDesigns(params: StampDesignsListParams) {
  return useQuery({
    queryKey: stampDesignKeys.list(params),
    queryFn: () => listStampDesigns(params),
  })
}

export function useStampDesign(id: string | undefined) {
  return useQuery({
    queryKey: stampDesignKeys.detail(id ?? ''),
    queryFn: () => getStampDesign(id!),
    enabled: Boolean(id),
  })
}

export function useCreateStampDesign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreateStampDesignRequest) => createStampDesign(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: stampDesignKeys.lists() })
    },
  })
}

export function useUpdateStampDesign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateStampDesignRequest }) =>
      updateStampDesign(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: stampDesignKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: stampDesignKeys.detail(variables.id) })
    },
  })
}

export function useDeleteStampDesign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteStampDesign(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: stampDesignKeys.lists() })
    },
  })
}
