import type { MilestoneResponse } from '../../../types/milestones'
import type { PartnerResponse } from '../../../types/partners'
import { shortenId } from '../../stamp-designs/utils/resolve-labels'

export function resolveMilestoneLabel(
  milestoneId: string,
  milestones: MilestoneResponse[],
): { label: string; unknown: boolean } {
  const milestone = milestones.find((m) => m.id === milestoneId)
  if (!milestone) {
    return {
      label: `${shortenId(milestoneId)} — Unknown milestone`,
      unknown: true,
    }
  }
  return {
    label: `${milestone.name} (${milestone.code})`,
    unknown: false,
  }
}

export function resolvePartnerLabel(
  partnerId: string | undefined,
  partners: PartnerResponse[],
): { label: string; unknown: boolean } {
  if (!partnerId) {
    return { label: '—', unknown: false }
  }

  const partner = partners.find((p) => p.id === partnerId)
  if (!partner) {
    return {
      label: `${shortenId(partnerId)} — Unknown partner`,
      unknown: true,
    }
  }
  return {
    label: partner.name,
    unknown: false,
  }
}

export function buildMilestoneOptions(milestones: MilestoneResponse[]) {
  return milestones.map((m) => ({
    value: m.id,
    label: `${m.name} (${m.code})`,
  }))
}

export function buildPartnerOptions(partners: PartnerResponse[]) {
  return [
    { value: '', label: '(No partner — internal reward)' },
    ...partners.map((p) => ({
      value: p.id,
      label: p.name,
    })),
  ]
}

export function formatRewardValue(valueAmount: number | undefined): string {
  if (valueAmount === null || valueAmount === undefined) {
    return '—'
  }
  if (valueAmount === 0) {
    return '—'
  }
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(valueAmount)
}

export function formatExpiryDays(expiryDays: number | undefined): string {
  if (expiryDays === null || expiryDays === undefined) {
    return '—'
  }
  return `${expiryDays} day${expiryDays === 1 ? '' : 's'}`
}

export function formatStock(totalStock: number | undefined, issuedCount: number | undefined): string {
  const stock = totalStock ?? 0
  const issued = issuedCount ?? 0
  if (stock === 0 && issued === 0) {
    return '—'
  }
  return `${issued}/${stock}`
}

export function canDisableVoucher(status: string): boolean {
  return status === 'AVAILABLE' || status === 'ASSIGNED'
}
