import type { ActiveStateFilter } from '../../components/filters'

export type PartnerActiveFilter = ActiveStateFilter

export interface PartnerFilters {
  active: PartnerActiveFilter
}

export const EMPTY_PARTNER_FILTERS: PartnerFilters = {
  active: 'ALL',
}
