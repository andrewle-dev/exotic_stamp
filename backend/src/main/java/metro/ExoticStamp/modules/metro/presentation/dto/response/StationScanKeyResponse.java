package metro.ExoticStamp.modules.metro.presentation.dto.response;

import lombok.Builder;
import lombok.Value;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;
import metro.ExoticStamp.modules.metro.presentation.dto.StationScanKeyStatusApi;

import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class StationScanKeyResponse {
    UUID id;
    UUID stationId;
    ScanTypeApi scanType;
    String keyPrefix;
    String payloadScheme;
    String label;
    String placementNote;
    StationScanKeyStatusApi status;
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
