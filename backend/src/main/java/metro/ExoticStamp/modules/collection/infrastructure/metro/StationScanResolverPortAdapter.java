package metro.ExoticStamp.modules.collection.infrastructure.metro;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.port.StationScanResolverPort;
import metro.ExoticStamp.modules.collection.application.view.ResolvedStationView;
import metro.ExoticStamp.modules.metro.application.MetroScanResolveService;
import metro.ExoticStamp.modules.metro.application.command.ScanResolveCommand;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveStationView;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StationScanResolverPortAdapter implements StationScanResolverPort {

    private final MetroScanResolveService metroScanResolveService;

    @Override
    public ResolvedStationView resolve(String scanType, String payload) {
        ScanResolveView result = metroScanResolveService.resolve(ScanResolveCommand.builder()
                .scanType(scanType)
                .payload(payload)
                .build());
        ScanResolveStationView station = result.getStation();
        return ResolvedStationView.builder()
                .id(station.getId())
                .code(station.getCode())
                .name(station.getName())
                .displayName(station.getDisplayName())
                .lineId(station.getLineId())
                .lineCode(station.getLineCode())
                .lineName(station.getLineName())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .zoneRadiusMeters(station.getZoneRadiusMeters())
                .imageUrl(station.getImageUrl())
                .stampPreviewUrl(station.getStampPreviewUrl())
                .scanType(result.getScanType())
                .build();
    }
}
