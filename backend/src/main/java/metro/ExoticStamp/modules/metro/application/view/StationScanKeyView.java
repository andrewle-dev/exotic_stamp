package metro.ExoticStamp.modules.metro.application.view;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class StationScanKeyView {
    UUID id;
    UUID stationId;
    String scanType;
    String keyPrefix;
    String payloadScheme;
    String label;
    String placementNote;
    String status;
    LocalDateTime activatedAt;
    LocalDateTime revokedAt;
    UUID replacedById;
    LocalDateTime lastSeenAt;
    LocalDateTime lastInstallVerifiedAt;
    Double installedLatitude;
    Double installedLongitude;
    Double installedAccuracyMeters;
    String installedDevicePlatform;
    String installedAppVersion;
    UUID installedBy;
    UUID createdBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
