export type ContractStatus =
  | 'NO_CONTRACT'
  | 'EXPIRED'
  | 'EXPIRING_SOON'
  | 'ACTIVE_CONTRACT'
  | 'FUTURE_CONTRACT'

const EXPIRING_SOON_DAYS = 30

function parseDateOnly(value: string | undefined): Date | null {
  if (!value?.trim()) {
    return null
  }
  const date = new Date(`${value.trim()}T00:00:00`)
  return Number.isNaN(date.getTime()) ? null : date
}

function startOfToday(): Date {
  const now = new Date()
  return new Date(now.getFullYear(), now.getMonth(), now.getDate())
}

function daysBetween(from: Date, to: Date): number {
  const msPerDay = 24 * 60 * 60 * 1000
  return Math.round((to.getTime() - from.getTime()) / msPerDay)
}

export function deriveContractStatus(
  contractStartDate?: string,
  contractEndDate?: string,
): ContractStatus {
  const start = parseDateOnly(contractStartDate)
  const end = parseDateOnly(contractEndDate)
  const today = startOfToday()

  if (!start && !end) {
    return 'NO_CONTRACT'
  }

  if (end && end.getTime() < today.getTime()) {
    return 'EXPIRED'
  }

  if (end) {
    const daysLeft = daysBetween(today, end)
    if (daysLeft >= 0 && daysLeft <= EXPIRING_SOON_DAYS) {
      return 'EXPIRING_SOON'
    }
  }

  if (start && start.getTime() > today.getTime()) {
    return 'FUTURE_CONTRACT'
  }

  return 'ACTIVE_CONTRACT'
}

export function contractDaysRemaining(contractEndDate?: string): number | null {
  const end = parseDateOnly(contractEndDate)
  if (!end) {
    return null
  }
  return daysBetween(startOfToday(), end)
}
