package metro.ExoticStamp.infra.storage;

/**
 * Maps upload purpose to storage object category for key generation.
 */
public final class AssetUploadPurposeMapper {

    private AssetUploadPurposeMapper() {
    }

    public static StorageObjectCategory toCategory(AssetUploadPurpose purpose) {
        if (purpose == null) {
            return StorageObjectCategory.TEMPORARY;
        }
        return switch (purpose) {
            case STATION_COVER, STATION_CARD -> StorageObjectCategory.STATION_COVER;
            case STAMP_ARTWORK, STAMP_PREVIEW -> StorageObjectCategory.STAMP_DESIGN;
            case PARTNER_LOGO -> StorageObjectCategory.PARTNER_LOGO;
            case PARTNER_BANNER -> StorageObjectCategory.PARTNER_BANNER;
            case CAMPAIGN_BANNER, CAMPAIGN_THUMBNAIL -> StorageObjectCategory.CAMPAIGN;
            case MILESTONE_REWARD -> StorageObjectCategory.REWARD;
            case GENERIC -> StorageObjectCategory.TEMPORARY;
        };
    }
}
