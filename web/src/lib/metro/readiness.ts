import type { StationDetailResponse, StationResponse } from '../../types/stations'

type GpsFields = Pick<StationResponse, 'latitude' | 'longitude' | 'zoneRadiusMeters'>

export function isGpsReady(station: GpsFields): boolean {
  return (
    station.latitude !== null &&
    station.latitude !== undefined &&
    station.longitude !== null &&
    station.longitude !== undefined &&
    station.zoneRadiusMeters !== null &&
    station.zoneRadiusMeters !== undefined
  )
}

export function gpsReadinessStatus(station: GpsFields): 'GPS_OK' | 'GPS_MISSING' {
  return isGpsReady(station) ? 'GPS_OK' : 'GPS_MISSING'
}

export function scanKeyConfigured(detail: StationDetailResponse): boolean {
  const hasKey = Boolean(detail.nfcTagId?.trim() || detail.qrCodeValue?.trim())
  return hasKey && detail.scanKeyStatus === 'ACTIVE'
}

export function scanKeyReadinessStatus(
  detail: StationDetailResponse,
): 'CONFIGURED' | 'MISSING' {
  return scanKeyConfigured(detail) ? 'CONFIGURED' : 'MISSING'
}
