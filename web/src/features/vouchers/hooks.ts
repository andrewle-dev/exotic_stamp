import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { disableVoucher, getVoucher, importVouchers, listVouchers } from '../../lib/api/vouchers.api'
import { analyticsKeys } from '../../lib/query/keys/analytics'
import { rewardKeys } from '../../lib/query/keys/rewards'
import { voucherKeys } from '../../lib/query/keys/vouchers'
import { invalidateKeys } from '../../lib/query/invalidate'
import type {
  ImportVouchersRequest,
  VoucherPoolResponse,
  VouchersListParams,
} from '../../types/vouchers'

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

function cacheVoucherDetail(
  queryClient: ReturnType<typeof useQueryClient>,
  voucher: VoucherPoolResponse,
) {
  queryClient.setQueryData(voucherKeys.detail(voucher.id), voucher)
}

export function useDisableVoucher() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id }: { id: string; rewardId?: string }) => disableVoucher(id),
    onSuccess: async (voucher, variables) => {
      cacheVoucherDetail(queryClient, voucher)
      const keys = [voucherKeys.lists(), analyticsKeys.collectionStats()] as const
      await invalidateKeys(queryClient, [...keys])
      if (variables.rewardId) {
        await invalidateKeys(queryClient, [
          rewardKeys.voucherStats(variables.rewardId),
          rewardKeys.detail(variables.rewardId),
          rewardKeys.lists(),
        ])
      } else {
        // Disable from voucher pool may not know reward id — refresh reward aggregates.
        await invalidateKeys(queryClient, [rewardKeys.all])
      }
    },
  })
}

export function useImportVouchers() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: ImportVouchersRequest) => importVouchers(payload),
    onSuccess: async () => {
      await invalidateKeys(queryClient, [
        voucherKeys.lists(),
        rewardKeys.all,
        analyticsKeys.collectionStats(),
      ])
    },
  })
}
