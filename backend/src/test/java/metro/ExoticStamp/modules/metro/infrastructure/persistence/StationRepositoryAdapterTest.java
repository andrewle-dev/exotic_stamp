package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationRepositoryAdapterTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    @Mock private JpaStationRepository jpaStationRepository;
    @InjectMocks private StationRepositoryAdapter adapter;

    @Test
    void findByNfcTagId_delegatesAndReturnsSameEntity() {
        Station station = Station.builder().id(STATION_ID).nfcTagId("NFC_1")
                .status(MetroStatus.ACTIVE).scanKeyStatus(ScanKeyStatus.ACTIVE)
                .lineId(LINE_ID).code("S1").name("S").sortOrder(1).collectorCount(0)
                .createdAt(LocalDateTime.now()).build();
        when(jpaStationRepository.findByNfcTagId("NFC_1")).thenReturn(Optional.of(station));
        assertEquals(station, adapter.findByNfcTagId("NFC_1").orElseThrow());
    }

    @Test
    void findAllByLineIdAndStatus_ordersBySortOrder() {
        List<Station> expected = List.of(Station.builder().id(STATION_ID).lineId(LINE_ID).code("S1")
                .name("S").sortOrder(1).status(MetroStatus.ACTIVE).scanKeyStatus(ScanKeyStatus.INACTIVE)
                .collectorCount(0).createdAt(LocalDateTime.now()).build());
        when(jpaStationRepository.findAllByLineIdAndStatusOrderBySortOrderAsc(LINE_ID, MetroStatus.ACTIVE))
                .thenReturn(expected);
        assertEquals(expected, adapter.findAllByLineIdAndStatus(LINE_ID, MetroStatus.ACTIVE));
        verify(jpaStationRepository).findAllByLineIdAndStatusOrderBySortOrderAsc(LINE_ID, MetroStatus.ACTIVE);
    }
}
