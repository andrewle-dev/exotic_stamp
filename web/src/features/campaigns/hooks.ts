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
import { analyticsKeys } from '../../lib/query/keys/analytics'
import { campaignKeys } from '../../lib/query/keys/campaigns'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  AssignCampaignStationRequest,
  CampaignResponse,
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

function cacheCampaignDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  campaign: CampaignResponse,
) {
  queryClient.setQueryData(campaignKeys.detail(campaign.id), campaign)
}

export function useCreateCampaign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (body: CreateCampaignRequest) => createCampaign(body),
    onSuccess: async (campaign) => {
      cacheCampaignDetail(queryClient, campaign)
      await invalidateKeys(queryClient, [
        campaignKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useUpdateCampaign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateCampaignRequest }) =>
      updateCampaign(id, body),
    onSuccess: async (campaign) => {
      cacheCampaignDetail(queryClient, campaign)
      await invalidateKeys(queryClient, [
        campaignKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
    },
  })
}

export function useDeleteCampaign() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deleteCampaign(id),
    onSuccess: async (_data, id) => {
      queryClient.removeQueries({ queryKey: campaignKeys.detail(id) })
      queryClient.removeQueries({ queryKey: campaignKeys.stations(id) })
      await invalidateKeys(queryClient, [
        campaignKeys.lists(),
        analyticsKeys.collectionStats(),
      ])
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
    onSuccess: async (_data, variables) => {
      await invalidateKeys(queryClient, [
        campaignKeys.stations(variables.campaignId),
        campaignKeys.detail(variables.campaignId),
        campaignKeys.lists(),
        analyticsKeys.collectionStats(),
        analyticsKeys.stationStats(),
      ])
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
    onSuccess: async (_data, variables) => {
      await invalidateKeys(queryClient, [
        campaignKeys.stations(variables.campaignId),
        campaignKeys.detail(variables.campaignId),
        campaignKeys.lists(),
        analyticsKeys.collectionStats(),
        analyticsKeys.stationStats(),
      ])
    },
  })
}
