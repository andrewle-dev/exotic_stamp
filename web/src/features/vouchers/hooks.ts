import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { disableVoucher, getVoucher, importVouchers, listVouchers } from '../../lib/api/vouchers.api'
import { rewardKeys } from '../../lib/query/keys/rewards'
import { voucherKeys } from '../../lib/query/keys/vouchers'
import type { ImportVouchersRequest, VouchersListParams } from '../../types/vouchers'

export function useVouchers(params: VouchersListParams) {
  return useQuery({
    queryKey: voucherKeys.list(params),
    queryFn: () => listVouchers(params),
  })
}

export function useVoucher(id: string | undefined) {
  return useQuery({
    queryKey: voucherKeys.detail(id ?? ''),
    queryFn: () => getVoucher(id!),
    enabled: Boolean(id),
  })
}

export function useDisableVoucher() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id }: { id: string; rewardId?: string }) => disableVoucher(id),
    onSuccess: (_data, variables) => {
      void queryClient.invalidateQueries({ queryKey: voucherKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: voucherKeys.detail(variables.id) })
      if (variables.rewardId) {
        void queryClient.invalidateQueries({
          queryKey: rewardKeys.voucherStats(variables.rewardId),
        })
      }
    },
  })
}

export function useImportVouchers() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: ImportVouchersRequest) => importVouchers(payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: voucherKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.lists() })
      void queryClient.invalidateQueries({ queryKey: rewardKeys.all })
    },
  })
}
