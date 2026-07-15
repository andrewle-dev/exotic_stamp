package metro.ExoticStamp.modules.metro.application.mapper;

import metro.ExoticStamp.modules.metro.application.view.StationScanKeyCreatedView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyVerifyView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyView;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import org.springframework.stereotype.Component;

@Component
public class StationScanKeyAppMapper {

    public StationScanKeyView toView(StationScanKey key) {
        return StationScanKeyView.builder()
                .id(key.getId())
                .stationId(key.getStationId())
                .scanType(key.getScanType() != null ? key.getScanType().name() : null)
                .keyPrefix(key.getKeyPrefix())
                .payloadScheme(key.getPayloadScheme())
                .label(key.getLabel())
                .placementNote(key.getPlacementNote())
                .status(key.getStatus() != null ? key.getStatus().name() : null)
                .activatedAt(key.getActivatedAt())
                .revokedAt(key.getRevokedAt())
                .replacedById(key.getReplacedById())
                .lastSeenAt(key.getLastSeenAt())
                .lastInstallVerifiedAt(key.getLastInstallVerifiedAt())
                .installedLatitude(key.getInstalledLatitude())
                .installedLongitude(key.getInstalledLongitude())
                .installedAccuracyMeters(key.getInstalledAccuracyMeters())
                .installedDevicePlatform(key.getInstalledDevicePlatform())
                .installedAppVersion(key.getInstalledAppVersion())
                .installedBy(key.getInstalledBy())
                .createdBy(key.getCreatedBy())
                .createdAt(key.getCreatedAt())
                .updatedAt(key.getUpdatedAt())
                .build();
    }

    public StationScanKeyCreatedView toCreatedView(StationScanKey key, String payloadToWrite) {
        return StationScanKeyCreatedView.builder()
                .id(key.getId())
                .stationId(key.getStationId())
                .scanType(key.getScanType() != null ? key.getScanType().name() : null)
                .payloadToWrite(payloadToWrite)
                .keyPrefix(key.getKeyPrefix())
                .status(key.getStatus() != null ? key.getStatus().name() : null)
                .label(key.getLabel())
                .placementNote(key.getPlacementNote())
                .build();
    }

    public StationScanKeyVerifyView toVerifyView(StationScanKey key) {
        return StationScanKeyVerifyView.builder()
                .verified(true)
                .id(key.getId())
                .stationId(key.getStationId())
                .lastInstallVerifiedAt(key.getLastInstallVerifiedAt())
                .build();
    }
}
