import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createMetroLine,
  deleteMetroLine,
  getMetroLine,
  listMetroLines,
  updateMetroLine,
} from '../../lib/api/metro-lines.api'
import { metroLineKeys } from '../../lib/query/keys/metro-lines'
import type { MetroLinesListParams } from '../../types/metro-lines'
import type { CreateLineRequest, UpdateLineRequest } from '../../types/metro-lines'

export function useMetroLinesList(params: MetroLinesListParams) {
  return useQuery({
    queryKey: metroLineKeys.list(params),
    queryFn: () => listMetroLines(params),
  })
}

export function useMetroLineDetail(id: string | undefined) {
  return useQuery({
    queryKey: metroLineKeys.detail(id ?? ''),
    queryFn: () => getMetroLine(id!),
    enabled: Boolean(id),
  })
}

export function useCreateMetroLine() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateLineRequest) => createMetroLine(body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: metroLineKeys.lists() })
    },
  })
}

export function useUpdateMetroLine() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateLineRequest }) =>
      updateMetroLine(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: metroLineKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: metroLineKeys.detail(variables.id) })
    },
  })
}

export function useDeleteMetroLine() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteMetroLine(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: metroLineKeys.lists() })
    },
  })
}
