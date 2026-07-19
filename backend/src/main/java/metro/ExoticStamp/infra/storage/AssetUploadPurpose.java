package metro.ExoticStamp.infra.storage;

/**
 * Purpose of a public asset upload. Controls dimension / aspect validation rules.
 * GENERIC keeps legacy type+size-only behavior for backward compatibility.
 */
public enum AssetUploadPurpose {
    GENERIC,
    STAMP_ARTWORK,
    STAMP_PREVIEW,
    PARTNER_LOGO,
    PARTNER_BANNER,
    STATION_COVER,
    STATION_CARD,
    CAMPAIGN_BANNER,
    CAMPAIGN_THUMBNAIL,
    MILESTONE_REWARD;

    public static AssetUploadPurpose fromParam(String raw) {
        if (raw == null || raw.isBlank()) {
            return GENERIC;
        }
        try {
            return AssetUploadPurpose.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown upload purpose: " + raw);
        }
    }
}
