import type { VouchersListParams } from '../../../types/vouchers'

export const voucherKeys = {
  all: ['vouchers'] as const,
  lists: () => [...voucherKeys.all, 'list'] as const,
  list: (params: VouchersListParams) => [...voucherKeys.lists(), params] as const,
  details: () => [...voucherKeys.all, 'detail'] as const,
  detail: (id: string) => [...voucherKeys.details(), id] as const,
}
