import type { CampaignResponse } from '../../../types/campaigns'
import type { StationResponse } from '../../../types/stations'

export function shortenId(id: string): string {
  if (id.length <= 8) {
    return id
  }
  return `${id.slice(0, 8)}…`
}

export function resolveCampaignLabel(
  campaignId: string,
  campaigns: CampaignResponse[],
): { label: string; unknown: boolean } {
  const campaign = campaigns.find((c) => c.id === campaignId)
  if (!campaign) {
    return {
      label: `${shortenId(campaignId)} — Unknown campaign`,
      unknown: true,
    }
  }
  const name = campaign.displayName || campaign.name
  return {
    label: `${name} (${campaign.code})`,
    unknown: false,
  }
}

export function resolveStationLabel(
  stationId: string,
  stations: StationResponse[],
): { label: string; unknown: boolean } {
  const station = stations.find((s) => s.id === stationId)
  if (!station) {
    return {
      label: `${shortenId(stationId)} — Unknown station`,
      unknown: true,
    }
  }
  const line = station.lineCode ?? station.lineName
  const parts = [station.code, station.name]
  if (line) {
    parts.push(line)
  }
  return {
    label: parts.join(' · '),
    unknown: false,
  }
}

export function buildCampaignOptions(campaigns: CampaignResponse[]) {
  return campaigns.map((c) => ({
    value: c.id,
    label: `${c.displayName || c.name} (${c.code})`,
  }))
}

export function buildStationOptions(stations: StationResponse[]) {
  return stations.map((s) => {
    const line = s.lineCode ?? s.lineName
    const suffix = line ? ` · ${line}` : ''
    return {
      value: s.id,
      label: `${s.code} — ${s.name}${suffix}`,
    }
  })
}
