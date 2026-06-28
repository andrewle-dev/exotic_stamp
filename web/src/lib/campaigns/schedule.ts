import type { CampaignScheduleState } from '../../types/campaigns'

export function getCampaignScheduleState(
  startAt: string,
  endAt: string,
  now: Date = new Date(),
): CampaignScheduleState {
  const start = new Date(startAt)
  const end = new Date(endAt)
  const time = now.getTime()

  if (time < start.getTime()) {
    return 'UPCOMING'
  }
  if (time > end.getTime()) {
    return 'EXPIRED'
  }
  return 'RUNNING'
}

export function formatDateRange(startAt: string, endAt: string): string {
  const start = new Date(startAt)
  const end = new Date(endAt)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime())) {
    return '—'
  }

  const dateFmt = new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' })
  return `${dateFmt.format(start)} – ${dateFmt.format(end)}`
}
