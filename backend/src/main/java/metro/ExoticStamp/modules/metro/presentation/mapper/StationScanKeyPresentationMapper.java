package metro.ExoticStamp.modules.metro.presentation.mapper;

import metro.ExoticStamp.modules.metro.application.command.ActivateStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.CreateStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.RevokeStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.VerifyStationScanKeyInstallationCommand;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyCreatedView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyVerifyView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyView;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;
import metro.ExoticStamp.modules.metro.presentation.dto.StationScanKeyStatusApi;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateStationScanKeyRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.RevokeStationScanKeyRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.VerifyStationScanKeyInstallationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationScanKeyCreatedResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationScanKeyResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationScanKeyVerifyResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StationScanKeyPresentationMapper {

    public CreateStationScanKeyCommand toCreateCommand(UUID stationId, CreateStationScanKeyRequest request) {
        return CreateStationScanKeyCommand.builder()
                .stationId(stationId)
                .scanType(request.getScanType() != null ? request.getScanType().name() : null)
                .label(request.getLabel())
                .placementNote(request.getPlacementNote())
                .build();
    }

    public ActivateStationScanKeyCommand toActivateCommand(UUID id) {
        return ActivateStationScanKeyCommand.builder().id(id).build();
    }

    public RevokeStationScanKeyCommand toRevokeCommand(UUID id, RevokeStationScanKeyRequest request) {
        return RevokeStationScanKeyCommand.builder()
                .id(id)
                .reason(request != null ? request.getReason() : null)
                .build();
    }

    public VerifyStationScanKeyInstallationCommand toVerifyCommand(
            UUID id, VerifyStationScanKeyInstallationRequest request) {
        return VerifyStationScanKeyInstallationCommand.builder()
                .id(id)
                .payloadReadBack(request.getPayloadReadBack())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracyMeters(request.getAccuracyMeters())
                .devicePlatform(request.getDevicePlatform())
                .appVersion(request.getAppVersion())
                .build();
    }

    public StationScanKeyResponse toResponse(StationScanKeyView view) {
        return StationScanKeyResponse.builder()
                .id(view.getId())
                .stationId(view.getStationId())
                .scanType(parseScanType(view.getScanType()))
                .keyPrefix(view.getKeyPrefix())
                .payloadScheme(view.getPayloadScheme())
                .label(view.getLabel())
                .placementNote(view.getPlacementNote())
                .status(parseStatus(view.getStatus()))
                .activatedAt(view.getActivatedAt())
                .revokedAt(view.getRevokedAt())
                .replacedById(view.getReplacedById())
                .lastSeenAt(view.getLastSeenAt())
                .lastInstallVerifiedAt(view.getLastInstallVerifiedAt())
                .installedLatitude(view.getInstalledLatitude())
                .installedLongitude(view.getInstalledLongitude())
                .installedAccuracyMeters(view.getInstalledAccuracyMeters())
                .installedDevicePlatform(view.getInstalledDevicePlatform())
                .installedAppVersion(view.getInstalledAppVersion())
                .installedBy(view.getInstalledBy())
                .createdBy(view.getCreatedBy())
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }

    public StationScanKeyCreatedResponse toCreatedResponse(StationScanKeyCreatedView view) {
        return StationScanKeyCreatedResponse.builder()
                .id(view.getId())
                .stationId(view.getStationId())
                .scanType(parseScanType(view.getScanType()))
                .payloadToWrite(view.getPayloadToWrite())
                .keyPrefix(view.getKeyPrefix())
                .status(parseStatus(view.getStatus()))
                .label(view.getLabel())
                .placementNote(view.getPlacementNote())
                .build();
    }

    public StationScanKeyVerifyResponse toVerifyResponse(StationScanKeyVerifyView view) {
        return StationScanKeyVerifyResponse.builder()
                .verified(view.isVerified())
                .id(view.getId())
                .stationId(view.getStationId())
                .lastInstallVerifiedAt(view.getLastInstallVerifiedAt())
                .build();
    }

    private static ScanTypeApi parseScanType(String value) {
        return value == null ? null : ScanTypeApi.valueOf(value);
    }

    private static StationScanKeyStatusApi parseStatus(String value) {
        return value == null ? null : StationScanKeyStatusApi.valueOf(value);
    }
}
