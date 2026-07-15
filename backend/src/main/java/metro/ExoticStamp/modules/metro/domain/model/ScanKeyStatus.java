package metro.ExoticStamp.modules.metro.domain.model;

/**
 * Lifecycle status for {@link StationScanKey}.
 * Station-level {@code stations.scan_key_status} only uses {@link #ACTIVE} and {@link #INACTIVE}.
 */
public enum ScanKeyStatus {
    DRAFT,
    ACTIVE,
    INACTIVE,
    REVOKED,
    LOST,
    REPLACED
}
