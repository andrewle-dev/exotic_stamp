package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationQueryServiceTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    @Mock private LineRepository lineRepository;
    @Mock private StationRepository stationRepository;
    @Mock private MetroAppMapper mapper;

    private StationQueryService service;

    @BeforeEach
    void setUp() {
        service = new StationQueryService(lineRepository, stationRepository, mapper);
    }

    @Test
    void getPublicStations_onlyActiveUnderActiveLines() {
        Line line = activeLine();
        Station station = activeStation();
        when(stationRepository.findAllActiveUnderActiveLines()).thenReturn(List.of(station));
        when(lineRepository.findAllByIdIn(any())).thenReturn(List.of(line));
        when(mapper.toStationView(eq(station), any())).thenReturn(StationView.builder().id(STATION_ID).build());

        List<StationView> result = service.getPublicStations(null);
        assertEquals(1, result.size());
    }

    @Test
    void resolveStationViewByNfc_inactive_throws() {
        Station station = activeStation();
        station.setStatus(MetroStatus.INACTIVE);
        when(stationRepository.findByNfcTagId("NFC1")).thenReturn(Optional.of(station));
        assertThrows(StationInactiveException.class, () -> service.resolveStationViewByNfc("NFC1"));
    }

    @Test
    void getPublicStationDetail_inactive_returns404() {
        Station station = activeStation();
        station.setStatus(MetroStatus.INACTIVE);
        Line line = activeLine();
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        assertThrows(StationNotFoundException.class, () -> service.getPublicStationDetail(STATION_ID));
    }

    private static Line activeLine() {
        return Line.builder().id(LINE_ID).code("L1").name("Line").sortOrder(0)
                .totalStations(1).status(MetroStatus.ACTIVE).createdAt(LocalDateTime.now()).build();
    }

    private static Station activeStation() {
        return Station.builder().id(STATION_ID).lineId(LINE_ID).code("S1").name("S").sortOrder(1)
                .status(MetroStatus.ACTIVE).collectorCount(0).createdAt(LocalDateTime.now()).build();
    }
}
