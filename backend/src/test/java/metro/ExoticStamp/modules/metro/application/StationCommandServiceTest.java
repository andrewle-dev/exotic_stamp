package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.infra.storage.asset.AssetLifecycleService;
import metro.ExoticStamp.modules.metro.application.command.CreateStationCommand;
import metro.ExoticStamp.modules.metro.application.command.ReorderStationsCommand;
import metro.ExoticStamp.modules.metro.application.command.RotateStationQrCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateScanKeysCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.domain.event.StationQrRotatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateNfcTagException;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidStationStatusException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.common.reorder.ReorderConflictException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationCommandServiceTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    @Mock private LineRepository lineRepository;
    @Mock private StationRepository stationRepository;
    @Mock private StationCachePort stationCachePort;
    @Mock private MetroAppMapper mapper;
    @Mock private StorageService storageService;
    @Mock private AssetLifecycleService assetLifecycleService;
    @Mock private StationImagePointerService stationImagePointerService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MetroAuditHelper metroAuditHelper;

    private StationCommandService stationCommandService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.getFile().setMaxSizeMb(5);
        props.getFile().setAllowedTypes(List.of("image/jpeg", "image/png", "image/webp"));
        stationCommandService = new StationCommandService(
                lineRepository, stationRepository, stationCachePort, mapper,
                storageService, new FileValidator(props), assetLifecycleService,
                stationImagePointerService, eventPublisher, metroAuditHelper);
    }

    @Test
    void createStation_success() {
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        when(stationRepository.existsByLineIdAndSortOrder(LINE_ID, 1)).thenReturn(false);
        when(stationRepository.existsByLineIdAndCode(LINE_ID, "S1")).thenReturn(false);
        when(stationRepository.existsByNfcTagId("NFC1")).thenReturn(false);
        when(stationRepository.existsByQrCodeValue("QR1")).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> {
            Station s = inv.getArgument(0);
            s.setId(STATION_ID);
            return s;
        });
        when(lineRepository.save(any(Line.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toStationDetailView(any(Station.class), any(Line.class), eq(true)))
                .thenReturn(StationDetailView.builder().id(STATION_ID).build());

        stationCommandService.createStation(CreateStationCommand.builder()
                .lineId(LINE_ID).code("S1").name("Station").sortOrder(1)
                .status("ACTIVE").nfcTagId("NFC1").qrCodeValue("QR1")
                .latitude(BigDecimal.ONE).longitude(BigDecimal.ONE).build());

        verify(stationRepository).save(any(Station.class));
    }

        @Test
        void createStation_withoutSortOrder_assignsNextSequence() {
                when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
                Station existing = activeStation();
                existing.setSortOrder(0);
                when(stationRepository.findAllByLineId(LINE_ID)).thenReturn(List.of(existing));
                when(stationRepository.existsByLineIdAndSortOrder(LINE_ID, 1)).thenReturn(false);
                when(stationRepository.existsByLineIdAndCode(LINE_ID, "S2")).thenReturn(false);
                when(stationRepository.save(any(Station.class))).thenAnswer(inv -> {
                        Station station = inv.getArgument(0);
                        station.setId(STATION_ID);
                        return station;
                });
                when(mapper.toStationDetailView(any(Station.class), any(Line.class), eq(true)))
                                .thenReturn(StationDetailView.builder().id(STATION_ID).build());

                stationCommandService.createStation(CreateStationCommand.builder()
                                .lineId(LINE_ID).code("S2").name("Second station").build());

                ArgumentCaptor<Station> captor = ArgumentCaptor.forClass(Station.class);
                verify(stationRepository).save(captor.capture());
                assertEquals(1, captor.getValue().getSortOrder());
        }

    @Test
    void createStation_lineNotFound_throws() {
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.empty());
        assertThrows(LineNotFoundException.class, () -> stationCommandService.createStation(
                CreateStationCommand.builder().lineId(LINE_ID).code("S1").name("N").sortOrder(1)
                        .status("ACTIVE").build()));
    }

    @Test
    void createStation_duplicateNfc_throws() {
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        when(stationRepository.existsByLineIdAndSortOrder(LINE_ID, 1)).thenReturn(false);
        when(stationRepository.existsByLineIdAndCode(LINE_ID, "S1")).thenReturn(false);
        when(stationRepository.existsByNfcTagId("NFC_DUP")).thenReturn(true);
        assertThrows(DuplicateNfcTagException.class, () -> stationCommandService.createStation(
                CreateStationCommand.builder().lineId(LINE_ID).code("S1").name("Station").sortOrder(1)
                        .status("ACTIVE").nfcTagId("NFC_DUP").build()));
    }

    @Test
    void createStation_activeUnderInactiveLine_throws() {
        Line draftLine = activeLine();
        draftLine.setStatus(MetroStatus.DRAFT);
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(draftLine));
        assertThrows(InvalidStationStatusException.class, () -> stationCommandService.createStation(
                CreateStationCommand.builder().lineId(LINE_ID).code("S1").name("Station").sortOrder(1)
                        .status("ACTIVE").build()));
    }

    @Test
    void rotateQr_publishesEventAndEvictsOldToken() {
        Station st = activeStation();
        st.setQrCodeValue("OLD_QR");
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        when(stationRepository.existsByQrCodeValueAndIdNot(any(), eq(STATION_ID))).thenReturn(false);
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        when(mapper.toStationDetailView(any(), any(), eq(true)))
                .thenReturn(StationDetailView.builder().id(STATION_ID).build());

        stationCommandService.rotateQr(RotateStationQrCommand.builder().stationId(STATION_ID).build());

        verify(stationCachePort).evictByQrToken("OLD_QR");
        ArgumentCaptor<StationQrRotatedEvent> cap = ArgumentCaptor.forClass(StationQrRotatedEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(STATION_ID, cap.getValue().stationId());
        assertEquals("OLD_QR", cap.getValue().oldQrToken());
    }

    @Test
    void deleteStation_evictsCaches() {
        Station st = activeStation();
        st.setNfcTagId("NFC");
        st.setQrCodeValue("QR");
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        when(lineRepository.save(any(Line.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        stationCommandService.deleteStation(STATION_ID);

        verify(stationCachePort).evictDetailByStationId(STATION_ID);
        verify(stationCachePort).evictByNfcTagId("NFC");
        verify(stationCachePort).evictByQrToken("QR");
    }

    @Test
    void incrementCollectorCount_inactiveStation_throws() {
        Station st = activeStation();
        st.setStatus(MetroStatus.INACTIVE);
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        assertThrows(StationInactiveException.class, () -> stationCommandService.incrementCollectorCount(STATION_ID));
    }

    @Test
    void uploadImage_invalidType_throws() {
        MockMultipartFile file = new MockMultipartFile("file", "a.gif", "image/gif", new byte[10]);
        assertThrows(InvalidImageTypeException.class, () -> stationCommandService.uploadStationImage(STATION_ID, file));
    }

    @Test
    void updateScanKeys_duplicateNfc_throws() {
        Station st = activeStation();
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(st));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        when(stationRepository.existsByNfcTagIdAndIdNot("NFC_DUP", STATION_ID)).thenReturn(true);
        assertThrows(DuplicateNfcTagException.class, () -> stationCommandService.updateScanKeys(
                UpdateScanKeysCommand.builder().stationId(STATION_ID).nfcTagId("NFC_DUP").build()));
    }

    @Test
    void reorderStations_twoPhaseDenseRenumber() {
        UUID s2 = UUID.fromString("00000000-0000-0000-0000-000000000502");
        Station a = Station.builder().id(STATION_ID).lineId(LINE_ID).code("S1").name("A").sortOrder(0)
                .collectorCount(0).status(MetroStatus.ACTIVE).scanKeyStatus(ScanKeyStatus.INACTIVE)
                .createdAt(LocalDateTime.now()).build();
        Station b = Station.builder().id(s2).lineId(LINE_ID).code("S2").name("B").sortOrder(1)
                .collectorCount(0).status(MetroStatus.DRAFT).scanKeyStatus(ScanKeyStatus.INACTIVE)
                .createdAt(LocalDateTime.now()).build();
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        when(stationRepository.findAllByLineId(LINE_ID)).thenReturn(List.of(a, b));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = stationCommandService.reorderStations(
                ReorderStationsCommand.builder()
                        .lineId(LINE_ID)
                        .orderedIds(List.of(s2, STATION_ID))
                        .build());

        assertEquals(2, result.updatedCount());
        assertEquals(LINE_ID, result.scopeId());
        assertEquals(0, b.getSortOrder());
        assertEquals(1, a.getSortOrder());
        verify(stationRepository).flush();
        verify(stationRepository, times(4)).save(any(Station.class));
    }

    @Test
    void reorderStations_incompleteSet_throwsConflict() {
        Station a = activeStation();
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        when(stationRepository.findAllByLineId(LINE_ID)).thenReturn(List.of(a));

        assertThrows(ReorderConflictException.class, () ->
                stationCommandService.reorderStations(
                        ReorderStationsCommand.builder()
                                .lineId(LINE_ID)
                                .orderedIds(List.of())
                                .build()));
    }

    private static Line activeLine() {
        return Line.builder().id(LINE_ID).code("L1").name("Line").sortOrder(0)
                .totalStations(0).status(MetroStatus.ACTIVE).createdAt(LocalDateTime.now()).build();
    }

    private static Station activeStation() {
        return Station.builder().id(STATION_ID).lineId(LINE_ID).code("S1").name("S").sortOrder(1)
                .collectorCount(0).status(MetroStatus.ACTIVE).scanKeyStatus(ScanKeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now()).build();
    }
}
