package metro.ExoticStamp.modules.collection.domain;

public final class CollectionAuditConstants {

    private CollectionAuditConstants() {}

    public static final String TABLE_CAMPAIGNS = "campaigns";
    public static final String TABLE_CAMPAIGN_STATIONS = "campaign_stations";
    public static final String TABLE_STAMP_DESIGNS = "stamp_designs";
    public static final String TABLE_USER_STAMPS = "user_stamps";

    public static final String CAMPAIGN_CREATED = "CAMPAIGN_CREATED";
    public static final String CAMPAIGN_UPDATED = "CAMPAIGN_UPDATED";
    public static final String CAMPAIGN_ACTIVATED = "CAMPAIGN_ACTIVATED";
    public static final String CAMPAIGN_ARCHIVED = "CAMPAIGN_ARCHIVED";

    public static final String CAMPAIGN_STATION_ASSIGNED = "CAMPAIGN_STATION_ASSIGNED";
    public static final String CAMPAIGN_STATION_REMOVED = "CAMPAIGN_STATION_REMOVED";

    public static final String STAMP_DESIGN_CREATED = "STAMP_DESIGN_CREATED";
    public static final String STAMP_DESIGN_UPDATED = "STAMP_DESIGN_UPDATED";
    public static final String STAMP_DESIGN_DISABLED = "STAMP_DESIGN_DISABLED";

    public static final String STAMP_COLLECTED = "STAMP_COLLECTED";
    public static final String STAMP_DUPLICATE_ATTEMPT = "STAMP_DUPLICATE_ATTEMPT";
    public static final String GPS_VALIDATION_FAILED = "GPS_VALIDATION_FAILED";
}
