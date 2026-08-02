package metro.ExoticStamp.modules.collection.infrastructure.metro;

import metro.ExoticStamp.modules.metro.application.MetroScanResolveService;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveStationView;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationScanResolverPortAdapterTest {

    @Mock private MetroScanResolveService metroScanResolveService;

    @InjectMocks private StationScanResolverPortAdapter adapter;

    @Test
    void resolve_mapsMetroViewToCollectionView() {
        UUID stationId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        ScanResolveStationView station = ScanResolveStationView.builder()
                .id(stationId)
                .code("S1")
                .name("Central")
                .displayName("Central Metro")
                .lineId(lineId)
                .lineCode("L1")
                .lineName("Line 1")
                .latitude(BigDecimal.valueOf(10.0))
                .longitude(BigDecimal.valueOf(106.0))
                .zoneRadiusMeters(100)
                .imageUrl("https://cdn/station.png")
                .stampPreviewUrl("https://cdn/stamp.png")
                .build();
        when(metroScanResolveService.resolve(any())).thenReturn(
                ScanResolveView.builder().scanType("NFC").station(station).build());

        var resolved = adapter.resolve("NFC", "payload-data");

        assertEquals(stationId, resolved.id());
        assertEquals("NFC", resolved.scanType());
        assertEquals("Line 1", resolved.lineName());
        assertEquals("https://cdn/stamp.png", resolved.stampPreviewUrl());
    }
}
