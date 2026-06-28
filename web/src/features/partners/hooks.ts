import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  activatePartner,
  createPartner,
  deactivatePartner,
  getPartner,
  listPartners,
  updatePartner,
} from '../../lib/api/partners.api'
import { partnerKeys } from '../../lib/query/keys/partners'
import type {
  CreatePartnerRequest,
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

export function useCreatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: CreatePartnerRequest) => createPartner(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: partnerKeys.lists() })
    },
  })
}

export function useUpdatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdatePartnerRequest }) =>
      updatePartner(id, body),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: partnerKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: partnerKeys.detail(variables.id) })
    },
  })
}

export function useActivatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => activatePartner(id),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: partnerKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: partnerKeys.detail(id) })
    },
  })
}

export function useDeactivatePartner() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => deactivatePartner(id),
    onSuccess: (_data, id) => {
      void queryClient.invalidateQueries({ queryKey: partnerKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: partnerKeys.detail(id) })
    },
  })
}
