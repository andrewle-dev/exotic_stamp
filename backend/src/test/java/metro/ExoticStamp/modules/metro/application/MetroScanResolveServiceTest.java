package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.modules.metro.application.command.ScanResolveCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.support.ScanKeyHasher;
import metro.ExoticStamp.modules.metro.application.support.ScanPayloadParser;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import metro.ExoticStamp.modules.metro.domain.exception.LineInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationScanKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetroScanResolveServiceTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID SCAN_KEY_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");

    @Mock private StationRepository stationRepository;
    @Mock private LineRepository lineRepository;
    @Mock private StationScanKeyRepository stationScanKeyRepository;
    @Mock private StationScanKeyCommandService stationScanKeyCommandService;
    @Mock private MetroAppMapper mapper;

    private ScanKeyHasher scanKeyHasher;
    private MetroScanResolveService service;

    @BeforeEach
    void setUp() {
        scanKeyHasher = new ScanKeyHasher("");
        service = new MetroScanResolveService(
                stationRepository,
                lineRepository,
                stationScanKeyRepository,
                stationScanKeyCommandService,
                new ScanPayloadParser(),
                scanKeyHasher,
                mapper);
    }

    @Test
    void resolveNfc_legacyFallback_success() {
        Station station = activeStation();
        Line line = activeLine();
        when(stationScanKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("NFC1")).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        when(mapper.toScanResolveView(any(), any(), any())).thenReturn(
                ScanResolveView.builder().resolved(true).scanType("NFC").build());

        ScanResolveView result = service.resolve(ScanResolveCommand.builder()
                .scanType("NFC").payload("NFC1").build());
        assertTrue(result.isResolved());
        verify(stationScanKeyCommandService, never()).recordLastSeen(any());
    }

    @Test
    void resolveNfc_rawKey_viaStationScanKey() {
        Station station = activeStation();
        Line line = activeLine();
        StationScanKey key = activeScanKey("nfc_test_home_001");
        when(stationScanKeyRepository.findByKeyHash(scanKeyHasher.hash("nfc_test_home_001")))
                .thenReturn(Optional.of(key));
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        when(mapper.toScanResolveView(any(), any(), any())).thenReturn(
                ScanResolveView.builder().resolved(true).scanType("NFC").build());

        ScanResolveView result = service.resolve(ScanResolveCommand.builder()
                .scanType("NFC").payload("nfc_test_home_001").build());
        assertTrue(result.isResolved());
        verify(stationScanKeyCommandService).recordLastSeen(SCAN_KEY_ID);
    }

    @Test
    void resolveNfc_uriPayload_viaStationScanKey() {
        Station station = activeStation();
        Line line = activeLine();
        StationScanKey key = activeScanKey("nfc_test_home_001");
        when(stationScanKeyRepository.findByKeyHash(scanKeyHasher.hash("nfc_test_home_001")))
                .thenReturn(Optional.of(key));
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        when(mapper.toScanResolveView(any(), any(), any())).thenReturn(
                ScanResolveView.builder().resolved(true).scanType("NFC").build());

        ScanResolveView result = service.resolve(ScanResolveCommand.builder()
                .scanType("NFC")
                .payload("metrostamp://scan?k=nfc_test_home_001")
                .build());
        assertTrue(result.isResolved());
        verify(stationScanKeyCommandService).recordLastSeen(SCAN_KEY_ID);
    }

    @Test
    void resolveQr_success() {
        Station station = activeStation();
        Line line = activeLine();
        when(stationScanKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        when(stationRepository.findByQrCodeValue("QR1")).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        when(mapper.toScanResolveView(any(), any(), any())).thenReturn(
                ScanResolveView.builder().resolved(true).scanType("QR_STATIC").build());

        service.resolve(ScanResolveCommand.builder().scanType("QR_STATIC").payload("QR1").build());
    }

    @Test
    void blankPayload_throws() {
        assertThrows(InvalidScanPayloadException.class, () -> service.resolve(
                ScanResolveCommand.builder().scanType("NFC").payload("  ").build()));
    }

    @Test
    void unknownKey_throws() {
        when(stationScanKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("UNKNOWN")).thenReturn(Optional.empty());
        assertThrows(ScanKeyNotFoundException.class, () -> service.resolve(
                ScanResolveCommand.builder().scanType("NFC").payload("UNKNOWN").build()));
    }

    @Test
    void draftScanKey_throwsInactive() {
        StationScanKey key = activeScanKey("nfc_draft");
        key.setStatus(ScanKeyStatus.DRAFT);
        when(stationScanKeyRepository.findByKeyHash(scanKeyHasher.hash("nfc_draft")))
                .thenReturn(Optional.of(key));
        assertThrows(ScanKeyInactiveException.class, () -> service.resolve(
                ScanResolveCommand.builder().scanType("NFC").payload("nfc_draft").build()));
        verify(stationScanKeyCommandService, never()).recordLastSeen(any());
    }

    @Test
    void revokedScanKey_throwsInactive() {
        StationScanKey key = activeScanKey("nfc_revoked");
        key.setStatus(ScanKeyStatus.REVOKED);
        when(stationScanKeyRepository.findByKeyHash(scanKeyHasher.hash("nfc_revoked")))
                .thenReturn(Optional.of(key));
        assertThrows(ScanKeyInactiveException.class, () -> service.resolve(
                ScanResolveCommand.builder().scanType("NFC").payload("nfc_revoked").build()));
    }

    @Test
    void inactiveStation_throws() {
        Station station = activeStation();
        station.setStatus(MetroStatus.INACTIVE);
        when(stationScanKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("NFC1")).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        assertThrows(StationInactiveException.class, () -> service.resolve(
                ScanResolveCommand.builder().scanType("NFC").payload("NFC1").build()));
    }

    @Test
    void inactiveLine_throws() {
        Station station = activeStation();
        Line line = activeLine();
        line.setStatus(MetroStatus.INACTIVE);
        when(stationScanKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("NFC1")).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(line));
        assertThrows(LineInactiveException.class, () -> service.resolve(
                ScanResolveCommand.builder().scanType("NFC").payload("NFC1").build()));
    }

    @Test
    void inactiveScanKey_legacy_throws() {
        Station station = activeStation();
        station.setScanKeyStatus(ScanKeyStatus.INACTIVE);
        when(stationScanKeyRepository.findByKeyHash(any())).thenReturn(Optional.empty());
        when(stationRepository.findByNfcTagId("NFC1")).thenReturn(Optional.of(station));
        when(lineRepository.findById(LINE_ID)).thenReturn(Optional.of(activeLine()));
        assertThrows(ScanKeyInactiveException.class, () -> service.resolve(
                ScanResolveCommand.builder().scanType("NFC").payload("NFC1").build()));
    }

    private StationScanKey activeScanKey(String rawKey) {
        return StationScanKey.builder()
                .id(SCAN_KEY_ID)
                .stationId(STATION_ID)
                .scanType(ScanType.NFC)
                .keyHash(scanKeyHasher.hash(rawKey))
                .keyPrefix(rawKey.substring(0, Math.min(12, rawKey.length())))
                .payloadScheme(StationScanKey.DEFAULT_PAYLOAD_SCHEME)
                .status(ScanKeyStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
    }

    private static Line activeLine() {
        return Line.builder().id(LINE_ID).code("L1").name("Line").sortOrder(0)
                .totalStations(1).status(MetroStatus.ACTIVE).createdAt(LocalDateTime.now()).build();
    }

    private static Station activeStation() {
        return Station.builder().id(STATION_ID).lineId(LINE_ID).code("S1").name("Station")
                .sortOrder(1).status(MetroStatus.ACTIVE).scanKeyStatus(ScanKeyStatus.ACTIVE)
                .nfcTagId("NFC1").qrCodeValue("QR1").zoneRadiusMeters(150)
                .latitude(BigDecimal.ONE).longitude(BigDecimal.ONE)
                .collectorCount(0).createdAt(LocalDateTime.now()).build();
    }
}
