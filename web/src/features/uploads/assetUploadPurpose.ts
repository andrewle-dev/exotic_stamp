import type { AssetUploadPurpose } from '../../types/uploads'

export type { AssetUploadPurpose }

/** Canonical tooltip guidance for admin asset uploads. */
export const ASSET_UPLOAD_HELP = {
  stampArtwork:
    'Recommended: square image 1:1, ideally 2048×2048 or 2560×2560. Minimum: 1024×1024. Used in stamp book and collection screens.',
  stampPreview:
    'Optional compact preview. Falls back to main stamp artwork when empty. Recommended: square 1:1. Minimum: 512×512.',
  partnerLogo:
    'Recommended: square logo 1:1, ideally 1024×1024. Minimum: 512×512. PNG with transparent background is preferred.',
  partnerBanner:
    'Recommended: landscape banner 16:9, ideally 1920×1080. Minimum: 1280×720. Used for wide promotional/banner surfaces.',
  stationCover:
    'Recommended: square image 1:1, ideally 2048×2048 or 2560×2560. Minimum: 1024×1024. Used in station discovery and detail screens.',
  stationCard:
    'Recommended: square image 1:1. Minimum: 512×512. Used for station cards, lists, and previews.',
  campaignBanner:
    'Recommended: landscape banner 16:9, ideally 1920×1080. Minimum: 1280×720. Used for campaign promotional surfaces.',
  campaignThumbnail:
    'Recommended: square image 1:1, ideally 1024×1024. Minimum: 512×512. Used for campaign lists and cards.',
  milestoneReward:
    'Recommended: square image 1:1, ideally 1024×1024. Minimum: 512×512. Shown when the milestone unlocks.',
} as const
