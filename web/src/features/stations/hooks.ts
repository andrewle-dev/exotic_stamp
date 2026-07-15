import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createStation,
  deleteStation,
  getStation,
  getStationStats,
  listStations,
  reorderStations,
  rotateStationQr,
  updateStation,
  updateStationScanKeys,
} from '../../lib/api/stations.api'
import {
  activateStationScanKey,
  createStationScanKey,
  listStationScanKeys,
  markLostStationScanKey,
  revokeStationScanKey,
  verifyStationScanKeyInstallation,
} from '../../lib/api/station-scan-keys.api'
import { analyticsKeys } from '../../lib/query/keys/analytics'
import { stationKeys } from '../../lib/query/keys/stations'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  CreateStationRequest,
  CreateStationScanKeyRequest,
  RevokeStationScanKeyRequest,
  StationDetailResponse,
  StationsListParams,
  UpdateScanKeysRequest,
  UpdateStationRequest,
  VerifyStationScanKeyInstallationRequest,
} from '../../types/stations'
import type { ReorderStationsRequest } from '../../types/reorder'

export function useStationsList(params: StationsListParams, options?: { enabled?: boolean }) {
  return useQuery({
    queryKey: stationKeys.list(params),
    queryFn: () => listStations(params),
    enabled: options?.enabled ?? true,
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

async function invalidateStationCollections(queryClient: ReturnType<typeof useQueryClient>) {
  await invalidateKeys(queryClient, [
    stationKeys.lists(),
    stationKeys.stats(),
    analyticsKeys.stationStats(),
    analyticsKeys.collectionStats(),
  ])
}

function cacheStationDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  station: StationDetailResponse,
) {
  queryClient.setQueryData(stationKeys.detail(station.id), station)
}

export function useCreateStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateStationRequest) => createStation(body),
    onSuccess: async (station) => {
      cacheStationDetail(queryClient, station)
      await invalidateStationCollections(queryClient)
    },
  })
}

export function useUpdateStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateStationRequest }) =>
      updateStation(id, body),
    onSuccess: async (station) => {
      cacheStationDetail(queryClient, station)
      await invalidateStationCollections(queryClient)
    },
  })
}

export function useDeleteStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteStation(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: stationKeys.detail(id) })
      queryClient.removeQueries({ queryKey: stationKeys.scanKeys(id) })
      await invalidateStationCollections(queryClient)
    },
  })
}

export function useReorderStations() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: ReorderStationsRequest) => reorderStations(body),
    onSuccess: async () => {
      await invalidateKeys(queryClient, [stationKeys.lists()])
    },
  })
}

export function useUpdateScanKeys() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateScanKeysRequest }) =>
      updateStationScanKeys(id, body),
    onSuccess: async (station) => {
      cacheStationDetail(queryClient, station)
      await invalidateKeys(queryClient, [
        stationKeys.lists(),
        stationKeys.scanKeys(station.id),
        stationKeys.stats(),
        analyticsKeys.stationStats(),
      ])
    },
  })
}

export function useRotateStationQr() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => rotateStationQr(id),
    onSuccess: async (station) => {
      cacheStationDetail(queryClient, station)
      await invalidateKeys(queryClient, [
        stationKeys.lists(),
        stationKeys.scanKeys(station.id),
        stationKeys.stats(),
        analyticsKeys.stationStats(),
      ])
    },
  })
}

async function invalidateScanKeys(
  queryClient: ReturnType<typeof useQueryClient>,
  stationId: string,
) {
  await invalidateKeys(queryClient, [
    stationKeys.scanKeys(stationId),
    stationKeys.detail(stationId),
    stationKeys.lists(),
    stationKeys.stats(),
    analyticsKeys.stationStats(),
  ])
}

export function useStationScanKeys(stationId: string | undefined) {
  return useQuery({
    queryKey: stationKeys.scanKeys(stationId ?? ''),
    queryFn: () => listStationScanKeys(stationId!),
    enabled: Boolean(stationId),
  })
}

export function useCreateStationScanKey(stationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateStationScanKeyRequest) => createStationScanKey(stationId, body),
    onSuccess: async () => {
      await invalidateScanKeys(queryClient, stationId)
    },
  })
}

export function useActivateStationScanKey(stationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => activateStationScanKey(id),
    onSuccess: async () => {
      await invalidateScanKeys(queryClient, stationId)
    },
  })
}

export function useRevokeStationScanKey(stationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body?: RevokeStationScanKeyRequest }) =>
      revokeStationScanKey(id, body),
    onSuccess: async () => {
      await invalidateScanKeys(queryClient, stationId)
    },
  })
}

export function useMarkLostStationScanKey(stationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => markLostStationScanKey(id),
    onSuccess: async () => {
      await invalidateScanKeys(queryClient, stationId)
    },
  })
}

export function useVerifyStationScanKeyInstallation(stationId: string) {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      id,
      body,
    }: {
      id: string
      body: VerifyStationScanKeyInstallationRequest
    }) => verifyStationScanKeyInstallation(id, body),
    onSuccess: async () => {
      await invalidateScanKeys(queryClient, stationId)
    },
  })
}
