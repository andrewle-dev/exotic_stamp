package metro.ExoticStamp.modules.metro.domain;

public final class MetroAuditConstants {

    public static final String TABLE_LINES = "lines";
    public static final String TABLE_STATIONS = "stations";
    public static final String TABLE_STATION_SCAN_KEYS = "station_scan_keys";
    public static final String TABLE_UPLOADS = "uploads";

    public static final String LINE_CREATED = "LINE_CREATED";
    public static final String LINE_UPDATED = "LINE_UPDATED";
    public static final String LINE_DISABLED = "LINE_DISABLED";
    public static final String STATION_CREATED = "STATION_CREATED";
    public static final String STATION_UPDATED = "STATION_UPDATED";
    public static final String STATION_DISABLED = "STATION_DISABLED";
    public static final String SCAN_KEY_UPDATED = "SCAN_KEY_UPDATED";
    public static final String SCAN_KEY_CREATED = "SCAN_KEY_CREATED";
    public static final String SCAN_KEY_ACTIVATED = "SCAN_KEY_ACTIVATED";
    public static final String SCAN_KEY_REVOKED = "SCAN_KEY_REVOKED";
    public static final String SCAN_KEY_MARKED_LOST = "SCAN_KEY_MARKED_LOST";
    public static final String SCAN_KEY_INSTALL_VERIFIED = "SCAN_KEY_INSTALL_VERIFIED";
    public static final String QR_ROTATED = "QR_ROTATED";
    public static final String PUBLIC_ASSET_UPLOADED = "PUBLIC_ASSET_UPLOADED";


    private MetroAuditConstants() {
    }
}
