package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.modules.metro.application.mapper.StationScanKeyAppMapper;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyView;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationScanKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationScanKeyQueryServiceTest {

    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID KEY_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");

    @Mock private StationScanKeyRepository stationScanKeyRepository;
    @Mock private StationRepository stationRepository;

    private StationScanKeyQueryService service;

    @BeforeEach
    void setUp() {
        service = new StationScanKeyQueryService(
                stationScanKeyRepository, stationRepository, new StationScanKeyAppMapper());
    }

    @Test
    void list_doesNotExposePayloadOrRawKey() {
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(Station.builder().id(STATION_ID).build()));
        when(stationScanKeyRepository.findAllByStationIdOrderByCreatedAtDesc(STATION_ID)).thenReturn(List.of(
                StationScanKey.builder()
                        .id(KEY_ID)
                        .stationId(STATION_ID)
                        .scanType(ScanType.NFC)
                        .keyHash("deadbeef")
                        .keyPrefix("nfc_abcd")
                        .payloadScheme("metrostamp://scan")
                        .status(ScanKeyStatus.DRAFT)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .version(0L)
                        .build()));

        List<StationScanKeyView> views = service.listByStationId(STATION_ID);
        assertEquals(1, views.size());
        StationScanKeyView view = views.getFirst();
        assertEquals("nfc_abcd", view.getKeyPrefix());
        assertEquals("DRAFT", view.getStatus());
        // View type has no payloadToWrite / keyHash fields — metadata only
        assertNull(view.getLabel());
    }
}
