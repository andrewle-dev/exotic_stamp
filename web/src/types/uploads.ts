export type AssetUploadPurpose =
  | 'GENERIC'
  | 'STAMP_ARTWORK'
  | 'STAMP_PREVIEW'
  | 'PARTNER_LOGO'
  | 'PARTNER_BANNER'
  | 'STATION_COVER'
  | 'STATION_CARD'
  | 'CAMPAIGN_BANNER'
  | 'CAMPAIGN_THUMBNAIL'
  | 'MILESTONE_REWARD'

export interface PublicAssetUploadResponse {
  url: string
}
