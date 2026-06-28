import type { CampaignResponse } from '../../../types/campaigns'
import type { StationStatsResponse, StockStatus } from '../../../types/analytics'

export function shortenId(id: string): string {
  if (id.length <= 8) {
    return id
  }
  return `${id.slice(0, 8)}…`
}

export function resolveCampaignLabel(
  campaignId: string,
  campaigns: CampaignResponse[],
): string {
  const campaign = campaigns.find((entry) => entry.id === campaignId)
  if (!campaign) {
    return shortenId(campaignId)
  }

  return campaign.displayName || campaign.name
}

export function formatAnalyticsNumber(value: number | null | undefined): string {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '—'
  }

  return new Intl.NumberFormat().format(value)
}

export function percentageOfMax(value: number, max: number): number {
  if (max <= 0 || value <= 0) {
    return 0
  }

  return Math.min(100, Math.round((value / max) * 100))
}

export function calculateRemainingStock(
  totalStock: number | undefined,
  issuedCount: number | undefined,
): number | undefined {
  if (totalStock === undefined || totalStock === null) {
    return undefined
  }

  const issued = issuedCount ?? 0
  return Math.max(0, totalStock - issued)
}

export function calculateStockStatus(
  totalStock: number | undefined,
  issuedCount: number | undefined,
): StockStatus {
  if (totalStock === undefined || totalStock === null) {
    return 'UNKNOWN'
  }

  const issued = issuedCount ?? 0
  const remaining = totalStock - issued

  if (remaining <= 0) {
    return 'OUT_OF_STOCK'
  }

  if (remaining <= 5) {
    return 'LOW_STOCK'
  }

  return 'OK'
}

export function sortStationsByCollectors(
  stats: StationStatsResponse[],
): StationStatsResponse[] {
  return [...stats].sort((left, right) => right.collectorCount - left.collectorCount)
}
