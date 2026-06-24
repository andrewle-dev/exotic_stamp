package metro.ExoticStamp.modules.metro.application.mapper;

import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MetroAppMapperTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    private final MetroAppMapper mapper = new MetroAppMapper();

    @Test
    void toStationDetailView_public_excludesScanSecretsAndMetadata() {
        Station station = station();
        Line line = line();

        StationDetailView view = mapper.toStationDetailView(station, line, false);

        assertEquals(STATION_ID, view.getId());
        assertEquals("S1", view.getCode());
        assertNull(view.getNfcTagId());
        assertNull(view.getQrCodeValue());
        assertNull(view.getScanKeyStatus());
        assertNull(view.getLastQrRotatedAt());
        assertNull(view.getLastScanKeyUpdatedAt());
        assertNull(view.getCollectorCount());
    }

    @Test
    void toStationDetailView_admin_includesScanFields() {
        Station station = station();
        Line line = line();

        StationDetailView view = mapper.toStationDetailView(station, line, true);

        assertEquals("NFC1", view.getNfcTagId());
        assertEquals("QR1", view.getQrCodeValue());
        assertEquals("ACTIVE", view.getScanKeyStatus());
        assertEquals(3, view.getCollectorCount());
    }

    private static Line line() {
        return Line.builder().id(LINE_ID).code("L1").name("Line").sortOrder(0)
                .totalStations(1).status(MetroStatus.ACTIVE).createdAt(LocalDateTime.now()).build();
    }

    private static Station station() {
        return Station.builder().id(STATION_ID).lineId(LINE_ID).code("S1").name("Station")
                .sortOrder(1).status(MetroStatus.ACTIVE).scanKeyStatus(ScanKeyStatus.ACTIVE)
                .nfcTagId("NFC1").qrCodeValue("QR1").collectorCount(3)
                .createdAt(LocalDateTime.now()).build();
    }
}
