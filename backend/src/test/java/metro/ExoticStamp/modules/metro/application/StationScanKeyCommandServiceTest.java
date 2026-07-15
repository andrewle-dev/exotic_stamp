package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.modules.metro.application.command.ActivateStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.CreateStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.RevokeStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.VerifyStationScanKeyInstallationCommand;
import metro.ExoticStamp.modules.metro.application.mapper.StationScanKeyAppMapper;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.metro.application.support.ScanKeyHasher;
import metro.ExoticStamp.modules.metro.application.support.ScanPayloadParser;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyCreatedView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyVerifyView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyView;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyAlreadyActiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationScanKeyRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationScanKeyCommandServiceTest {

    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID KEY_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");
    private static final UUID ACTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-04T10:00:00Z"), ZoneOffset.UTC);

    @Mock private StationScanKeyRepository stationScanKeyRepository;
    @Mock private StationRepository stationRepository;
    @Mock private RbacSecurityContextHelper securityContextHelper;
    @Mock private MetroAuditHelper metroAuditHelper;

    private ScanKeyHasher scanKeyHasher;
    private ScanPayloadParser scanPayloadParser;
    private StationScanKeyCommandService service;

    @BeforeEach
    void setUp() {
        scanKeyHasher = new ScanKeyHasher("");
        scanPayloadParser = new ScanPayloadParser();
        service = new StationScanKeyCommandService(
                stationScanKeyRepository,
                stationRepository,
                scanKeyHasher,
                scanPayloadParser,
                new StationScanKeyAppMapper(),
                securityContextHelper,
                metroAuditHelper,
                CLOCK);
    }

    @Test
    void create_returnsPayloadOnce() {
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(Station.builder().id(STATION_ID).build()));
        when(securityContextHelper.currentUserId()).thenReturn(Optional.of(ACTOR_ID));
        when(stationScanKeyRepository.save(any())).thenAnswer(inv -> {
            StationScanKey key = inv.getArgument(0);
            key.setId(KEY_ID);
            return key;
        });

        StationScanKeyCreatedView created = service.create(CreateStationScanKeyCommand.builder()
                .stationId(STATION_ID)
                .scanType("NFC")
                .label("Gate A")
                .placementNote("Pillar 02")
                .build());

        assertEquals(KEY_ID, created.getId());
        assertEquals(STATION_ID, created.getStationId());
        assertEquals("NFC", created.getScanType());
        assertEquals("DRAFT", created.getStatus());
        assertEquals("Gate A", created.getLabel());
        assertNotNull(created.getPayloadToWrite());
        assertTrue(created.getPayloadToWrite().startsWith("metrostamp://scan?k=nfc_"));
        assertNotNull(created.getKeyPrefix());

        ArgumentCaptor<StationScanKey> captor = ArgumentCaptor.forClass(StationScanKey.class);
        verify(stationScanKeyRepository).save(captor.capture());
        StationScanKey saved = captor.getValue();
        assertEquals(ScanKeyStatus.DRAFT, saved.getStatus());
        assertEquals(scanKeyHasher.hash(scanPayloadParser.extractRawKey(created.getPayloadToWrite())),
                saved.getKeyHash());
    }

    @Test
    void create_unknownStation_throws() {
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.empty());
        assertThrows(StationNotFoundException.class, () -> service.create(CreateStationScanKeyCommand.builder()
                .stationId(STATION_ID)
                .scanType("NFC")
                .build()));
    }

    @Test
    void activate_draft_succeeds() {
        StationScanKey key = draftKey("nfc_abc");
        when(stationScanKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(key));
        when(stationScanKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StationScanKeyView view = service.activate(ActivateStationScanKeyCommand.builder().id(KEY_ID).build());
        assertEquals("ACTIVE", view.getStatus());
        assertNotNull(view.getActivatedAt());
    }

    @Test
    void activate_alreadyActive_throws() {
        StationScanKey key = draftKey("nfc_abc");
        key.setStatus(ScanKeyStatus.ACTIVE);
        when(stationScanKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(key));
        assertThrows(ScanKeyAlreadyActiveException.class, () ->
                service.activate(ActivateStationScanKeyCommand.builder().id(KEY_ID).build()));
    }

    @Test
    void revoke_setsRevoked() {
        StationScanKey key = draftKey("nfc_abc");
        key.setStatus(ScanKeyStatus.ACTIVE);
        when(stationScanKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(key));
        when(stationScanKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StationScanKeyView view = service.revoke(RevokeStationScanKeyCommand.builder()
                .id(KEY_ID).reason("damaged").build());
        assertEquals("REVOKED", view.getStatus());
        assertNotNull(view.getRevokedAt());
    }

    @Test
    void markLost_setsLost() {
        StationScanKey key = draftKey("nfc_abc");
        key.setStatus(ScanKeyStatus.ACTIVE);
        when(stationScanKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(key));
        when(stationScanKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StationScanKeyView view = service.markLost(KEY_ID);
        assertEquals("LOST", view.getStatus());
        assertNotNull(view.getRevokedAt());
    }

    @Test
    void verifyInstallation_matchingPayload_succeeds() {
        String rawKey = "nfc_verify_001";
        StationScanKey key = draftKey(rawKey);
        key.setStatus(ScanKeyStatus.ACTIVE);
        when(stationScanKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(key));
        when(securityContextHelper.currentUserId()).thenReturn(Optional.of(ACTOR_ID));
        when(stationScanKeyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        StationScanKeyVerifyView view = service.verifyInstallation(VerifyStationScanKeyInstallationCommand.builder()
                .id(KEY_ID)
                .payloadReadBack("metrostamp://scan?k=" + rawKey)
                .latitude(10.7721)
                .longitude(106.6983)
                .accuracyMeters(12.5)
                .devicePlatform("ANDROID")
                .appVersion("1.0.0")
                .build());

        assertTrue(view.isVerified());
        assertEquals(LocalDateTime.ofInstant(CLOCK.instant(), ZoneOffset.UTC), view.getLastInstallVerifiedAt());
        assertEquals(10.7721, key.getInstalledLatitude());
        assertEquals(106.6983, key.getInstalledLongitude());
        assertEquals(ACTOR_ID, key.getInstalledBy());
        verify(stationRepository, never()).save(any());
    }

    @Test
    void verifyInstallation_mismatch_throws() {
        StationScanKey key = draftKey("nfc_verify_001");
        when(stationScanKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(key));
        assertThrows(InvalidScanPayloadException.class, () ->
                service.verifyInstallation(VerifyStationScanKeyInstallationCommand.builder()
                        .id(KEY_ID)
                        .payloadReadBack("metrostamp://scan?k=nfc_other")
                        .build()));
    }

    @Test
    void revoke_terminal_throws() {
        StationScanKey key = draftKey("nfc_abc");
        key.setStatus(ScanKeyStatus.REVOKED);
        when(stationScanKeyRepository.findById(KEY_ID)).thenReturn(Optional.of(key));
        assertThrows(ScanKeyInactiveException.class, () ->
                service.revoke(RevokeStationScanKeyCommand.builder().id(KEY_ID).reason("x").build()));
    }

    private StationScanKey draftKey(String rawKey) {
        return StationScanKey.builder()
                .id(KEY_ID)
                .stationId(STATION_ID)
                .scanType(ScanType.NFC)
                .keyHash(scanKeyHasher.hash(rawKey))
                .keyPrefix(rawKey.substring(0, Math.min(12, rawKey.length())))
                .payloadScheme(StationScanKey.DEFAULT_PAYLOAD_SCHEME)
                .status(ScanKeyStatus.DRAFT)
                .createdAt(LocalDateTime.now(CLOCK))
                .updatedAt(LocalDateTime.now(CLOCK))
                .version(0L)
                .build();
    }
}
