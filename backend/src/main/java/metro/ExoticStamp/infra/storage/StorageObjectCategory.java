package metro.ExoticStamp.infra.storage;

/**
 * Logical object categories used to build versioned S3/local object keys.
 */
public enum StorageObjectCategory {
    STATION_COVER,
    STAMP_DESIGN,
    PARTNER_LOGO,
    PARTNER_BANNER,
    CAMPAIGN,
    REWARD,
    USER_PRIVATE,
    TEMPORARY,
    /** Legacy folder-style uploads mapped under public/temporary. */
    LEGACY_PUBLIC
}
