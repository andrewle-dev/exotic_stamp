package metro.ExoticStamp.modules.metro.presentation.mapper;

import metro.ExoticStamp.common.reorder.ReorderResponse;
import metro.ExoticStamp.common.reorder.ReorderResultView;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.metro.application.command.CreateLineCommand;
import metro.ExoticStamp.modules.metro.application.command.CreateStationCommand;
import metro.ExoticStamp.modules.metro.application.command.ReorderLinesCommand;
import metro.ExoticStamp.modules.metro.application.command.ReorderStationsCommand;
import metro.ExoticStamp.modules.metro.application.command.ScanResolveCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateLineCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateScanKeysCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateStationCommand;
import metro.ExoticStamp.modules.metro.application.view.LineDetailView;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.application.view.MetroPageSlice;
import metro.ExoticStamp.modules.metro.application.view.PublicAssetUploadView;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveStationView;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationStatsView;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.presentation.dto.MetroStatusApi;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanKeyStatusApi;
import metro.ExoticStamp.modules.metro.presentation.dto.ScanTypeApi;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ReorderLinesRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ReorderStationsRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ScanResolveRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateScanKeysRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.PublicAssetUploadResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.ScanResolveMetaResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.ScanResolveResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.ScanResolveStationResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationStatsResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MetroPresentationMapper {

    public CreateLineCommand toCreateLineCommand(CreateLineRequest request) {
        return CreateLineCommand.builder()
                .code(request.getCode())
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .colorHex(request.getColorHex())
                .sortOrder(request.getSortOrder())
                .status(enumName(request.getStatus()))
                .build();
    }

    public UpdateLineCommand toUpdateLineCommand(UUID lineId, UpdateLineRequest request) {
        return UpdateLineCommand.builder()
                .lineId(lineId)
                .code(request.getCode())
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .colorHex(request.getColorHex())
                .sortOrder(request.getSortOrder())
                .status(enumName(request.getStatus()))
                .build();
    }

    public ReorderLinesCommand toReorderLinesCommand(ReorderLinesRequest request) {
        return ReorderLinesCommand.builder()
                .orderedIds(request.getOrderedIds())
                .build();
    }

    public ReorderStationsCommand toReorderStationsCommand(ReorderStationsRequest request) {
        return ReorderStationsCommand.builder()
                .lineId(request.getLineId())
                .orderedIds(request.getOrderedIds())
                .build();
    }

    public CreateStationCommand toCreateStationCommand(CreateStationRequest request) {
        return CreateStationCommand.builder()
                .lineId(request.getLineId())
                .code(request.getCode())
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .address(request.getAddress())
                .sortOrder(request.getSortOrder())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .zoneRadiusMeters(request.getZoneRadiusMeters())
                .imageUrl(request.getImageUrl())
                .stampPreviewUrl(request.getStampPreviewUrl())
                .nfcTagId(request.getNfcTagId())
                .qrCodeValue(request.getQrCodeValue())
                .status(enumName(request.getStatus()))
                .build();
    }

    public UpdateStationCommand toUpdateStationCommand(UUID stationId, UpdateStationRequest request) {
        return UpdateStationCommand.builder()
                .stationId(stationId)
                .code(request.getCode())
                .name(request.getName())
                .displayName(request.getDisplayName())
                .description(request.getDescription())
                .address(request.getAddress())
                .sortOrder(request.getSortOrder())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .zoneRadiusMeters(request.getZoneRadiusMeters())
                .imageUrl(request.getImageUrl())
                .stampPreviewUrl(request.getStampPreviewUrl())
                .status(enumName(request.getStatus()))
                .build();
    }

    public UpdateScanKeysCommand toUpdateScanKeysCommand(UUID stationId, UpdateScanKeysRequest request) {
        return UpdateScanKeysCommand.builder()
                .stationId(stationId)
                .nfcTagId(request.getNfcTagId())
                .qrCodeValue(request.getQrCodeValue())
                .scanKeyStatus(enumName(request.getScanKeyStatus()))
                .build();
    }

    public ScanResolveCommand toScanResolveCommand(ScanResolveRequest request) {
        return ScanResolveCommand.builder()
                .scanType(enumName(request.getScanType()))
                .payload(request.getPayload())
                .devicePlatform(request.getDevicePlatform())
                .appVersion(request.getAppVersion())
                .build();
    }

    public LineResponse toResponse(LineView view) {
        return LineResponse.builder()
                .id(view.getId())
                .code(view.getCode())
                .name(view.getName())
                .displayName(view.getDisplayName())
                .description(view.getDescription())
                .colorHex(view.getColorHex())
                .sortOrder(view.getSortOrder())
                .totalStations(view.getTotalStations())
                .status(parseMetroStatus(view.getStatus()))
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }

    public List<LineResponse> toLineResponses(List<LineView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    public PageResponse<LineResponse> toLinePage(MetroPageSlice<LineView> slice) {
        return PageResponse.of(toLineResponses(slice.content()), slice.totalElements(), slice.totalPages(),
                slice.page(), slice.size());
    }

    public LineDetailResponse toResponse(LineDetailView view) {
        return LineDetailResponse.builder()
                .id(view.getId())
                .code(view.getCode())
                .name(view.getName())
                .displayName(view.getDisplayName())
                .description(view.getDescription())
                .colorHex(view.getColorHex())
                .sortOrder(view.getSortOrder())
                .totalStations(view.getTotalStations())
                .status(parseMetroStatus(view.getStatus()))
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .stations(toStationResponses(view.getStations()))
                .build();
    }

    public StationResponse toResponse(StationView view) {
        return StationResponse.builder()
                .id(view.getId())
                .lineId(view.getLineId())
                .lineCode(view.getLineCode())
                .lineName(view.getLineName())
                .code(view.getCode())
                .name(view.getName())
                .displayName(view.getDisplayName())
                .description(view.getDescription())
                .address(view.getAddress())
                .imageUrl(view.getImageUrl())
                .stampPreviewUrl(view.getStampPreviewUrl())
                .latitude(view.getLatitude())
                .longitude(view.getLongitude())
                .zoneRadiusMeters(view.getZoneRadiusMeters())
                .sortOrder(view.getSortOrder())
                .status(parseMetroStatus(view.getStatus()))
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }

    public List<StationResponse> toStationResponses(List<StationView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    public PageResponse<StationResponse> toStationPage(MetroPageSlice<StationView> slice) {
        return PageResponse.of(toStationResponses(slice.content()), slice.totalElements(), slice.totalPages(),
                slice.page(), slice.size());
    }

    public StationDetailResponse toResponse(StationDetailView view) {
        return StationDetailResponse.builder()
                .id(view.getId())
                .lineId(view.getLineId())
                .lineCode(view.getLineCode())
                .lineName(view.getLineName())
                .code(view.getCode())
                .name(view.getName())
                .displayName(view.getDisplayName())
                .description(view.getDescription())
                .address(view.getAddress())
                .imageUrl(view.getImageUrl())
                .stampPreviewUrl(view.getStampPreviewUrl())
                .latitude(view.getLatitude())
                .longitude(view.getLongitude())
                .zoneRadiusMeters(view.getZoneRadiusMeters())
                .sortOrder(view.getSortOrder())
                .status(parseMetroStatus(view.getStatus()))
                .nfcTagId(view.getNfcTagId())
                .qrCodeValue(view.getQrCodeValue())
                .scanKeyStatus(parseScanKeyStatus(view.getScanKeyStatus()))
                .lastQrRotatedAt(view.getLastQrRotatedAt())
                .lastScanKeyUpdatedAt(view.getLastScanKeyUpdatedAt())
                .createdAt(view.getCreatedAt())
                .updatedAt(view.getUpdatedAt())
                .build();
    }

    public ScanResolveResponse toResponse(ScanResolveView view) {
        ScanResolveStationView s = view.getStation();
        return ScanResolveResponse.builder()
                .station(ScanResolveStationResponse.builder()
                        .id(s.getId())
                        .code(s.getCode())
                        .name(s.getName())
                        .displayName(s.getDisplayName())
                        .lineId(s.getLineId())
                        .lineCode(s.getLineCode())
                        .lineName(s.getLineName())
                        .latitude(s.getLatitude())
                        .longitude(s.getLongitude())
                        .zoneRadiusMeters(s.getZoneRadiusMeters())
                        .imageUrl(s.getImageUrl())
                        .stampPreviewUrl(s.getStampPreviewUrl())
                        .build())
                .scan(ScanResolveMetaResponse.builder()
                        .scanType(parseScanType(view.getScanType()))
                        .resolved(view.isResolved())
                        .build())
                .build();
    }

    public StationStatsResponse toResponse(StationStatsView view) {
        return StationStatsResponse.builder()
                .stationId(view.getStationId())
                .stationName(view.getStationName())
                .lineName(view.getLineName())
                .collectorCount(view.getCollectorCount())
                .build();
    }

    public List<StationStatsResponse> toStationStatsResponses(List<StationStatsView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    public PublicAssetUploadResponse toResponse(PublicAssetUploadView view) {
        return new PublicAssetUploadResponse(view.getUrl());
    }

    public ReorderResponse toResponse(ReorderResultView view) {
        return ReorderResponse.from(view);
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static MetroStatusApi parseMetroStatus(String value) {
        return value == null ? null : MetroStatusApi.valueOf(value);
    }

    private static ScanKeyStatusApi parseScanKeyStatus(String value) {
        return value == null ? null : ScanKeyStatusApi.valueOf(value);
    }

    private static ScanTypeApi parseScanType(String value) {
        return value == null ? null : ScanTypeApi.valueOf(value);
    }
}
