import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  activatePartner,
  createPartner,
  deactivatePartner,
  getPartner,
  listPartners,
  updatePartner,
} from '../../lib/api/partners.api'
import { analyticsKeys } from '../../lib/query/keys/analytics'
import { partnerKeys } from '../../lib/query/keys/partners'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  CreatePartnerRequest,
  PartnerResponse,
  PartnersListParams,
  UpdatePartnerRequest,
} from '../../types/partners'

export function usePartners(params: PartnersListParams) {
  return useQuery({
    queryKey: partnerKeys.list(params),
    queryFn: () => listPartners(params),
  })
}

export function usePartner(id: string | undefined) {
  return useQuery({
    queryKey: partnerKeys.detail(id ?? ''),
    queryFn: () => getPartner(id!),
    enabled: Boolean(id),
  })
}

function cachePartnerDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  partner: PartnerResponse,
) {
  queryClient.setQueryData(partnerKeys.detail(partner.id), partner)
}

export function useCreatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreatePartnerRequest) => createPartner(payload),
    onSuccess: async (partner) => {
      cachePartnerDetail(queryClient, partner)
      await invalidateKeys(queryClient, [partnerKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}

export function useUpdatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdatePartnerRequest }) =>
      updatePartner(id, body),
    onSuccess: async (partner) => {
      cachePartnerDetail(queryClient, partner)
      await invalidateKeys(queryClient, [partnerKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}

export function useActivatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => activatePartner(id),
    onSuccess: async (partner) => {
      cachePartnerDetail(queryClient, partner)
      await invalidateKeys(queryClient, [partnerKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}

export function useDeactivatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deactivatePartner(id),
    onSuccess: async (partner) => {
      cachePartnerDetail(queryClient, partner)
      await invalidateKeys(queryClient, [partnerKeys.lists(), analyticsKeys.collectionStats()])
    },
  })
}
