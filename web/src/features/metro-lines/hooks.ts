import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createMetroLine,
  deleteMetroLine,
  getMetroLine,
  listMetroLines,
  reorderMetroLines,
  updateMetroLine,
} from '../../lib/api/metro-lines.api'
import { metroLineKeys } from '../../lib/query/keys/metro-lines'
import { stationKeys } from '../../lib/query/keys/stations'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  CreateLineRequest,
  LineResponse,
  MetroLinesListParams,
  UpdateLineRequest,
} from '../../types/metro-lines'
import type { ReorderLinesRequest } from '../../types/reorder'

export function useMetroLinesList(params: MetroLinesListParams, options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: metroLineKeys.list(params),
    queryFn: () => listMetroLines(params),
    enabled: options?.enabled ?? true,
  })
}

export function useMetroLineDetail(id: string | undefined) {
  return useQuery({
    queryKey: metroLineKeys.detail(id ?? ''),
    queryFn: () => getMetroLine(id!),
    enabled: Boolean(id),
  })
}

function cacheMetroLineDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  line: LineResponse,
) {
  queryClient.setQueryData(metroLineKeys.detail(line.id), line)
}

export function useCreateMetroLine() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateLineRequest) => createMetroLine(body),
    onSuccess: async (line) => {
      cacheMetroLineDetail(queryClient, line)
      await invalidateKeys(queryClient, [metroLineKeys.lists()])
    },
  })
}

export function useUpdateMetroLine() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateLineRequest }) =>
      updateMetroLine(id, body),
    onSuccess: async (line) => {
      cacheMetroLineDetail(queryClient, line)
      // Station rows show line code/name — refresh station lists after line edits.
      await invalidateKeys(queryClient, [metroLineKeys.lists(), stationKeys.lists()])
    },
  })
}

export function useDeleteMetroLine() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteMetroLine(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: metroLineKeys.detail(id) })
      await invalidateKeys(queryClient, [metroLineKeys.lists(), stationKeys.lists()])
    },
  })
}

export function useReorderMetroLines() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: ReorderLinesRequest) => reorderMetroLines(body),
    onSuccess: async () => {
      await invalidateKeys(queryClient, [metroLineKeys.lists()])
    },
  })
}
