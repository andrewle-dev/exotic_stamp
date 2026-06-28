import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createStation,
  deleteStation,
  getStation,
  getStationStats,
  listStations,
  rotateStationQr,
  updateStation,
  updateStationScanKeys,
} from '../../lib/api/stations.api'
import { stationKeys } from '../../lib/query/keys/stations'
import type {
  CreateStationRequest,
  StationsListParams,
  UpdateScanKeysRequest,
  UpdateStationRequest,
} from '../../types/stations'

export function useStationsList(params: StationsListParams) {
  return useQuery({
    queryKey: stationKeys.list(params),
    queryFn: () => listStations(params),
  })
}

export function useStationStats() {
  return useQuery({
    queryKey: stationKeys.stats(),
    queryFn: () => getStationStats(),
  })
}

export function useStationDetail(id: string | undefined) {
  return useQuery({
    queryKey: stationKeys.detail(id ?? ''),
    queryFn: () => getStation(id!),
    enabled: Boolean(id),
  })
}

export function useCreateStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateStationRequest) => createStation(body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: stationKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: stationKeys.stats() })
    },
  })
}

export function useUpdateStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateStationRequest }) =>
      updateStation(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: stationKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: stationKeys.detail(variables.id) })
      void queryClient.invalidateQueries({ queryKey: stationKeys.stats() })
    },
  })
}

export function useDeleteStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteStation(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: stationKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: stationKeys.stats() })
    },
  })
}

export function useUpdateScanKeys() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateScanKeysRequest }) =>
      updateStationScanKeys(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: stationKeys.detail(variables.id) })
    },
  })
}

export function useRotateStationQr() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => rotateStationQr(id),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: stationKeys.detail(id) })
    },
  })
}
