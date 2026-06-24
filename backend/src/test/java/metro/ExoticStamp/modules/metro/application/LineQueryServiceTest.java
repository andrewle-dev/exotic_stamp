package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.application.view.MetroPageSlice;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.MetroPageResult;
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
class LineQueryServiceTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Mock private LineRepository lineRepository;
    @Mock private StationRepository stationRepository;
    @Mock private MetroAppMapper mapper;

    private LineQueryService service;

    @BeforeEach
    void setUp() {
        service = new LineQueryService(lineRepository, stationRepository, mapper);
    }

    @Test
    void getPublicLines_onlyActive() {
        Line active = activeLine(MetroStatus.ACTIVE);
        when(lineRepository.findAllByStatus(MetroStatus.ACTIVE)).thenReturn(List.of(active));
        when(mapper.toLineView(active)).thenReturn(LineView.builder().id(LINE_ID).status("ACTIVE").build());

        List<LineView> result = service.getPublicLines();
        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.getFirst().getStatus());
    }

    @Test
    void getPublicLineDetail_inactiveLine_throws() {
        Line inactive = activeLine(MetroStatus.INACTIVE);
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(inactive));
        assertThrows(LineNotFoundException.class, () -> service.getPublicLineDetail(LINE_ID));
    }

    @Test
    void searchAdminLines_paginated() {
        Line line = activeLine(MetroStatus.ACTIVE);
        MetroPageResult<Line> page = new MetroPageResult<>(List.of(line), 1, 1, 0, 20);
        when(lineRepository.search(eq(MetroStatus.ACTIVE), eq("search"), eq(0), eq(20), eq("sortOrder"), eq(true)))
                .thenReturn(page);
        when(mapper.toLineView(line)).thenReturn(LineView.builder().id(LINE_ID).code("L1").build());

        MetroPageSlice<LineView> result = service.searchAdminLines("ACTIVE", "search", 0, 20, "sortOrder,asc");
        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
    }

    private static Line activeLine(MetroStatus status) {
        return Line.builder().id(LINE_ID).code("L1").name("Line").sortOrder(0)
                .totalStations(0).status(status).createdAt(LocalDateTime.now()).build();
    }
}
