export interface VoucherFilters {
  milestoneId: string
  status: string
}

export const EMPTY_VOUCHER_FILTERS: VoucherFilters = {
  milestoneId: '',
  status: '',
}
