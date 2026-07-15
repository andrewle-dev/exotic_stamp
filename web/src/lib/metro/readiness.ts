import type {
  StationDetailResponse,
  StationResponse,
  StationScanKeyResponse,
} from '../../types/stations'

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

export function scanKeyConfigured(
  detail: StationDetailResponse,
  productionKeys?: StationScanKeyResponse[],
): boolean {
  if (productionKeys?.some((key) => key.status === 'ACTIVE')) {
    return true
  }
  const hasLegacyKey = Boolean(detail.nfcTagId?.trim() || detail.qrCodeValue?.trim())
  return hasLegacyKey && detail.scanKeyStatus === 'ACTIVE'
}

export function scanKeyReadinessStatus(
  detail: StationDetailResponse,
  productionKeys?: StationScanKeyResponse[],
): 'CONFIGURED' | 'MISSING' {
  return scanKeyConfigured(detail, productionKeys) ? 'CONFIGURED' : 'MISSING'
}
