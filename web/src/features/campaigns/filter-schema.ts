import type { CampaignStatus, CampaignType } from '../../types/campaigns'

export type CampaignTypeFilter = CampaignType | 'ALL'
export type CampaignStatusFilter = CampaignStatus | 'ALL'

export interface CampaignFilters {
  type: CampaignTypeFilter
  status: CampaignStatusFilter
}

export const EMPTY_CAMPAIGN_FILTERS: CampaignFilters = {
  type: 'ALL',
  status: 'ALL',
}

export const CAMPAIGN_TYPE_LABELS: Record<Exclude<CampaignTypeFilter, 'ALL'>, string> = {
  STANDARD: 'Standard',
  SEASONAL: 'Seasonal',
  EVENT: 'Event',
}
