import type { CampaignResponse } from '../../../types/campaigns'
import type { CampaignStationResponse } from '../../../types/campaigns'
import type { PartnerResponse } from '../../../types/partners'
import type { RewardResponse } from '../../../types/rewards'
import type { StampDesignResponse } from '../../../types/stamp-designs'
import type { StationResponse } from '../../../types/stations'
import type { VoucherPoolResponse } from '../../../types/vouchers'
import type { OperationalWarning } from '../../../types/analytics'
import { maskVoucherCode } from '../../../lib/formatting/masking'
import { calculateStockStatus } from './helpers'

function hasGps(station: StationResponse): boolean {
  return station.latitude !== undefined && station.longitude !== undefined
}

function isBlank(value: string | undefined): boolean {
  return !value || value.trim().length === 0
}

function isVoucherExpired(voucher: VoucherPoolResponse): boolean {
  if (voucher.status === 'EXPIRED') {
    return true
  }

  if (!voucher.expiresAt) {
    return false
  }

  return new Date(voucher.expiresAt).getTime() < Date.now()
}

export interface OperationalWarningsInput {
  campaigns: CampaignResponse[]
  campaignStationsByCampaignId: Map<string, CampaignStationResponse[]>
  stations: StationResponse[]
  stampDesigns: StampDesignResponse[]
  rewards: RewardResponse[]
  vouchers: VoucherPoolResponse[]
  partners: PartnerResponse[]
}

export function deriveOperationalWarnings(input: OperationalWarningsInput): OperationalWarning[] {
  const warnings: OperationalWarning[] = []

  for (const campaign of input.campaigns) {
    if (campaign.status !== 'ACTIVE') {
      continue
    }

    const assignedStations = input.campaignStationsByCampaignId.get(campaign.id)
    if (assignedStations && assignedStations.length === 0) {
      warnings.push({
        severity: 'danger',
        category: 'Campaign',
        message: `Active campaign "${campaign.displayName || campaign.name}" has no assigned stations.`,
      })
    }
  }

  for (const station of input.stations) {
    if (!hasGps(station)) {
      warnings.push({
        severity: 'warning',
        category: 'Station',
        message: `Station "${station.name}" (${station.code}) is missing GPS coordinates.`,
      })
    }

    if (station.status === 'INACTIVE') {
      warnings.push({
        severity: 'warning',
        category: 'Station',
        message: `Station "${station.name}" (${station.code}) is inactive.`,
      })
    }
  }

  for (const design of input.stampDesigns) {
    if (isBlank(design.imageUrl) || isBlank(design.previewImageUrl)) {
      warnings.push({
        severity: 'warning',
        category: 'Stamp design',
        message: `Stamp design "${design.name}" is missing imageUrl or previewImageUrl.`,
      })
    }
  }

  for (const reward of input.rewards) {
    const stockStatus = calculateStockStatus(reward.totalStock, reward.issuedCount)
    if (stockStatus === 'OUT_OF_STOCK') {
      warnings.push({
        severity: 'danger',
        category: 'Reward',
        message: `Reward "${reward.name}" is out of stock (${reward.issuedCount ?? 0}/${reward.totalStock ?? 0} issued).`,
      })
    } else if (stockStatus === 'LOW_STOCK') {
      const remaining = (reward.totalStock ?? 0) - (reward.issuedCount ?? 0)
      warnings.push({
        severity: 'warning',
        category: 'Reward',
        message: `Reward "${reward.name}" has low stock (${remaining} remaining).`,
      })
    }
  }

  for (const voucher of input.vouchers) {
    if (voucher.status === 'DISABLED') {
      warnings.push({
        severity: 'warning',
        category: 'Voucher',
        message: `Voucher ${maskVoucherCode(voucher.code)} is disabled.`,
      })
      continue
    }

    if (isVoucherExpired(voucher)) {
      warnings.push({
        severity: 'warning',
        category: 'Voucher',
        message: `Voucher ${maskVoucherCode(voucher.code)} is expired.`,
      })
    }
  }

  for (const partner of input.partners) {
    if (isBlank(partner.logoUrl)) {
      warnings.push({
        severity: 'warning',
        category: 'Partner',
        message: `Partner "${partner.name}" is missing a logo.`,
      })
    }
  }

  return warnings
}
