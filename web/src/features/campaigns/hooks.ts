import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  assignCampaignStation,
  createCampaign,
  deleteCampaign,
  getCampaign,
  listCampaignStations,
  listCampaigns,
  removeCampaignStation,
  updateCampaign,
} from '../../lib/api/campaigns.api'
import { campaignKeys } from '../../lib/query/keys/campaigns'
import type {
  AssignCampaignStationRequest,
  CampaignsListParams,
  CreateCampaignRequest,
  UpdateCampaignRequest,
} from '../../types/campaigns'

export function useCampaigns(params: CampaignsListParams) {
  return useQuery({
    queryKey: campaignKeys.list(params),
    queryFn: () => listCampaigns(params),
  })
}

export function useCampaign(id: string | undefined) {
  return useQuery({
    queryKey: campaignKeys.detail(id ?? ''),
    queryFn: () => getCampaign(id!),
    enabled: Boolean(id),
  })
}

export function useCreateCampaign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateCampaignRequest) => createCampaign(body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: campaignKeys.lists() })
    },
  })
}

export function useUpdateCampaign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateCampaignRequest }) =>
      updateCampaign(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: campaignKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: campaignKeys.detail(variables.id) })
    },
  })
}

export function useDeleteCampaign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteCampaign(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: campaignKeys.lists() })
    },
  })
}

export function useCampaignStations(campaignId: string | undefined) {
  return useQuery({
    queryKey: campaignKeys.stations(campaignId ?? ''),
    queryFn: () => listCampaignStations(campaignId!),
    enabled: Boolean(campaignId),
  })
}

export function useAssignCampaignStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      campaignId,
      body,
    }: {
      campaignId: string
      body: AssignCampaignStationRequest
    }) => assignCampaignStation(campaignId, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: campaignKeys.stations(variables.campaignId) })
      void queryClient.invalidateQueries({ queryKey: campaignKeys.detail(variables.campaignId) })
    },
  })
}

export function useRemoveCampaignStation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({
      campaignId,
      stationId,
    }: {
      campaignId: string
      stationId: string
    }) => removeCampaignStation(campaignId, stationId),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: campaignKeys.stations(variables.campaignId) })
      void queryClient.invalidateQueries({ queryKey: campaignKeys.detail(variables.campaignId) })
    },
  })
}
