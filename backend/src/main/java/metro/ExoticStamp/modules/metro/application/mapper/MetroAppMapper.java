package metro.ExoticStamp.modules.metro.application.mapper;

import metro.ExoticStamp.modules.metro.application.view.LineDetailView;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveStationView;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MetroAppMapper {

    public LineView toLineView(Line line) {
        if (line == null) {
            return null;
        }
        return LineView.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .displayName(line.getDisplayName())
                .description(line.getDescription())
                .colorHex(line.getColorHex())
                .sortOrder(line.getSortOrder())
                .totalStations(line.getTotalStations())
                .status(line.getStatus() != null ? line.getStatus().name() : null)
                .createdAt(line.getCreatedAt())
                .updatedAt(line.getUpdatedAt())
                .build();
    }

    public LineDetailView toLineDetailView(Line line, List<StationView> stationViews) {
        if (line == null) {
            return null;
        }
        return LineDetailView.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .displayName(line.getDisplayName())
                .description(line.getDescription())
                .colorHex(line.getColorHex())
                .sortOrder(line.getSortOrder())
                .totalStations(line.getTotalStations())
                .status(line.getStatus() != null ? line.getStatus().name() : null)
                .createdAt(line.getCreatedAt())
                .updatedAt(line.getUpdatedAt())
                .stations(stationViews)
                .build();
    }

    public StationView toStationView(Station station, Line line) {
        if (station == null) {
            return null;
        }
        return StationView.builder()
                .id(station.getId())
                .lineId(station.getLineId())
                .lineCode(line != null ? line.getCode() : null)
                .lineName(line != null ? line.getName() : null)
                .code(station.getCode())
                .name(station.getName())
                .displayName(station.getDisplayName())
                .description(station.getDescription())
                .address(station.getAddress())
                .imageUrl(station.getImageUrl())
                .stampPreviewUrl(station.getStampPreviewUrl())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .zoneRadiusMeters(station.getZoneRadiusMeters())
                .sortOrder(station.getSortOrder())
                .status(station.getStatus() != null ? station.getStatus().name() : null)
                .createdAt(station.getCreatedAt())
                .updatedAt(station.getUpdatedAt())
                .build();
    }

    public StationDetailView toStationDetailView(Station station, Line line, boolean includeSensitive) {
        if (station == null) {
            return null;
        }
        StationDetailView.StationDetailViewBuilder builder = StationDetailView.builder()
                .id(station.getId())
                .lineId(station.getLineId())
                .lineCode(line != null ? line.getCode() : null)
                .lineName(line != null ? line.getName() : null)
                .code(station.getCode())
                .name(station.getName())
                .displayName(station.getDisplayName())
                .description(station.getDescription())
                .address(station.getAddress())
                .imageUrl(station.getImageUrl())
                .stampPreviewUrl(station.getStampPreviewUrl())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .zoneRadiusMeters(station.getZoneRadiusMeters())
                .sortOrder(station.getSortOrder())
                .status(station.getStatus() != null ? station.getStatus().name() : null)
                .createdAt(station.getCreatedAt())
                .updatedAt(station.getUpdatedAt());
        if (includeSensitive) {
            builder.nfcTagId(station.getNfcTagId())
                    .qrCodeValue(station.getQrCodeValue())
                    .scanKeyStatus(station.getScanKeyStatus() != null ? station.getScanKeyStatus().name() : null)
                    .lastQrRotatedAt(station.getLastQrRotatedAt())
                    .lastScanKeyUpdatedAt(station.getLastScanKeyUpdatedAt())
                    .collectorCount(station.getCollectorCount());
        }
        return builder.build();
    }

    public ScanResolveView toScanResolveView(Station station, Line line, ScanType scanType) {
        ScanResolveStationView stationView = ScanResolveStationView.builder()
                .id(station.getId())
                .code(station.getCode())
                .name(station.getName())
                .displayName(station.getDisplayName())
                .lineId(station.getLineId())
                .lineCode(line.getCode())
                .lineName(line.getName())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .zoneRadiusMeters(station.getZoneRadiusMeters())
                .imageUrl(station.getImageUrl())
                .stampPreviewUrl(station.getStampPreviewUrl())
                .build();
        return ScanResolveView.builder()
                .station(stationView)
                .scanType(scanType != null ? scanType.name() : null)
                .resolved(true)
                .build();
    }

    public static boolean isPubliclyVisible(Station station, Line line) {
        return station.getStatus() == MetroStatus.ACTIVE && line.getStatus() == MetroStatus.ACTIVE;
    }
}
