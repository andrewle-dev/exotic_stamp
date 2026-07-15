import type { StampDesignStatus, StampRarity } from '../../types/stamp-designs'

export type StampRarityFilter = StampRarity | 'ALL'
export type StampStatusFilter = StampDesignStatus | 'ALL'

/** Advanced filters schema for Stamp Designs (search lives in shared search state). */
export interface StampDesignFilters {
  campaignId: string
  stationId: string
  rarity: StampRarityFilter
  status: StampStatusFilter
}

export const EMPTY_STAMP_FILTERS: StampDesignFilters = {
  campaignId: '',
  stationId: '',
  rarity: 'ALL',
  status: 'ALL',
}

export const STAMP_RARITY_LABELS: Record<Exclude<StampRarityFilter, 'ALL'>, string> = {
  COMMON: 'Common',
  RARE: 'Rare',
  EPIC: 'Epic',
  LEGENDARY: 'Legendary',
}
