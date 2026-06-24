package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.port.StationScanResolverPort;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.service.CollectionCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.application.support.CollectionPolicyService;
import metro.ExoticStamp.modules.collection.application.support.CollectionRuntimeAuditHelper;
import metro.ExoticStamp.modules.collection.application.support.DefaultCampaignResolver;
import metro.ExoticStamp.modules.collection.application.support.GpsValidationService;
import metro.ExoticStamp.modules.collection.application.support.StampDesignResolver;
import metro.ExoticStamp.modules.collection.application.view.CollectStampResultView;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.ResolvedStationView;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.collection.domain.exception.GpsOutOfRangeException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsRequiredException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionCommandServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CAMPAIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID DESIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");
    private static final UUID STAMP_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");

    @Mock private StationScanResolverPort stationScanResolverPort;
    @Mock private DefaultCampaignResolver defaultCampaignResolver;
    @Mock private CampaignStationRepository campaignStationRepository;
    @Mock private StampDesignResolver stampDesignResolver;
    @Mock private GpsValidationService gpsValidationService;
    @Mock private CollectionPolicyService collectionPolicyService;
    @Mock private UserStampRepository userStampRepository;
    @Mock private StampDesignRepository stampDesignRepository;
    @Mock private CollectionQueryService collectionQueryService;
    @Mock private UserStampCachePort cachePort;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private CollectionRuntimeAuditHelper auditHelper;
    @Mock private StationReadPort stationReadPort;
    @Mock private LineReadPort lineReadPort;

    private Clock clock;
    private CollectionCommandService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-06-01T12:00:00Z"), ZoneOffset.UTC);
        service = new CollectionCommandService(
                stationScanResolverPort,
                defaultCampaignResolver,
                campaignStationRepository,
                stampDesignResolver,
                gpsValidationService,
                collectionPolicyService,
                userStampRepository,
                stampDesignRepository,
                collectionQueryService,
                cachePort,
                eventPublisher,
                auditHelper,
                stationReadPort,
                lineReadPort,
                clock
        );
    }

    @Test
    void collect_newStamp_success() {
        UUID idempotencyKey = UUID.fromString("00000000-0000-0000-0000-000000000999");
        ResolvedStationView station = defaultStation();
        Campaign campaign = defaultCampaign();
        StampDesign design = defaultDesign();

        when(collectionPolicyService.resolveIdempotentReplay(anyString(), eq(USER_ID))).thenReturn(Optional.empty());
        when(stationScanResolverPort.resolve("NFC", "NFC1")).thenReturn(station);
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(campaign);
        when(campaignStationRepository.exists(CAMPAIGN_ID, STATION_ID)).thenReturn(true);
        when(stampDesignResolver.resolveActive(CAMPAIGN_ID, STATION_ID)).thenReturn(design);
        when(gpsValidationService.validate(any(), any(), any(), eq(station)))
                .thenReturn(new GpsValidationService.GpsValidationResult(BigDecimal.TEN, BigDecimal.valueOf(35), true));
        when(userStampRepository.save(any(UserStamp.class))).thenAnswer(inv -> {
            UserStamp us = inv.getArgument(0);
            us.setId(STAMP_ID);
            return us;
        });
        when(collectionQueryService.computeProgress(USER_ID, LINE_ID, CAMPAIGN_ID))
                .thenReturn(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build());

        CollectStampResultView res = service.collect(defaultCommand(idempotencyKey));

        assertNotNull(res.stamp().stampId());
        assertTrue(res.isNew());
        assertEquals("Central", res.stamp().stationName());
        verify(cachePort).evictAllForUserCollection(USER_ID, LINE_ID, CAMPAIGN_ID);
        verify(defaultCampaignResolver).resolveActiveGlobalDefault(LINE_ID);

        ArgumentCaptor<StampCollectedEvent> cap = ArgumentCaptor.forClass(StampCollectedEvent.class);
        verify(eventPublisher).publishEvent(cap.capture());
        assertEquals(STAMP_ID, cap.getValue().getStampId());
        assertEquals(USER_ID, cap.getValue().getUserId());
        assertEquals(CollectMethod.NFC, cap.getValue().getCollectMethod());
    }

    @Test
    void collect_duplicate_throwsConflict() {
        UUID idempotencyKey = UUID.randomUUID();
        when(collectionPolicyService.resolveIdempotentReplay(anyString(), eq(USER_ID))).thenReturn(Optional.empty());
        when(stationScanResolverPort.resolve("QR_STATIC", "QR1")).thenReturn(defaultStation());
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(defaultCampaign());
        when(campaignStationRepository.exists(CAMPAIGN_ID, STATION_ID)).thenReturn(true);
        when(stampDesignResolver.resolveActive(CAMPAIGN_ID, STATION_ID)).thenReturn(defaultDesign());
        when(gpsValidationService.validate(any(), any(), any(), any()))
                .thenReturn(new GpsValidationService.GpsValidationResult(BigDecimal.ONE, BigDecimal.valueOf(30), true));
        doThrow(new StampAlreadyCollectedException(STATION_ID))
                .when(collectionPolicyService).assertCollectAllowed(USER_ID, STATION_ID, CAMPAIGN_ID);

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID, idempotencyKey, "QR_STATIC", "QR1",
                BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30),
                "ANDROID", "1.0.0", null);

        assertThrows(StampAlreadyCollectedException.class, () -> service.collect(cmd));
        verify(userStampRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void collect_inactiveStation_rejected() {
        UUID idempotencyKey = UUID.randomUUID();
        when(collectionPolicyService.resolveIdempotentReplay(anyString(), eq(USER_ID))).thenReturn(Optional.empty());
        when(stationScanResolverPort.resolve(eq("NFC"), eq("NFC1")))
                .thenThrow(new StationInactiveException(STATION_ID));

        assertThrows(StationInactiveException.class, () -> service.collect(defaultCommand(idempotencyKey)));
        verify(userStampRepository, never()).save(any());
    }

    @Test
    void collect_idempotencyKey_returnsExisting() {
        UUID idempotencyKey = UUID.randomUUID();
        UserStamp existing = UserStamp.builder()
                .userId(USER_ID)
                .stationId(STATION_ID)
                .campaignId(CAMPAIGN_ID)
                .stampDesignId(DESIGN_ID)
                .collectedAt(LocalDateTime.now(clock).minusMinutes(1))
                .gpsVerified(true)
                .collectMethod(CollectMethod.QR)
                .sourceScanType("QR_STATIC")
                .deviceFingerprint("device-fingerprint-123")
                .idempotencyKey(idempotencyKey.toString())
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN")
                .build();
        existing.setId(STAMP_ID);

        when(collectionPolicyService.resolveIdempotentReplay(idempotencyKey.toString(), USER_ID)).thenReturn(Optional.of(existing));
        when(stationReadPort.getStationViewById(STATION_ID))
                .thenReturn(MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());
        when(lineReadPort.getLineById(LINE_ID))
                .thenReturn(MetroLineView.builder().id(LINE_ID).code("L1").name("Line 1").active(true).build());
        when(stampDesignRepository.findById(DESIGN_ID))
                .thenReturn(Optional.of(StampDesign.builder().imageUrl("https://cdn/x.png").name("S")
                        .status(StampDesignStatus.ACTIVE).rarity(StampRarity.COMMON).sortOrder(0).isLimited(false).build()));
        when(collectionQueryService.computeProgress(USER_ID, LINE_ID, CAMPAIGN_ID))
                .thenReturn(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build());

        CollectStampResultView res = service.collect(defaultCommand(idempotencyKey));
        assertFalse(res.isNew());
        assertEquals(STAMP_ID, res.stamp().stampId());
        verify(userStampRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void collect_dataIntegrity_mapsToStampAlreadyCollected() {
        when(collectionPolicyService.resolveIdempotentReplay(anyString(), eq(USER_ID))).thenReturn(Optional.empty());
        when(stationScanResolverPort.resolve("NFC", "NFC1")).thenReturn(defaultStation());
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(defaultCampaign());
        when(campaignStationRepository.exists(CAMPAIGN_ID, STATION_ID)).thenReturn(true);
        when(stampDesignResolver.resolveActive(CAMPAIGN_ID, STATION_ID)).thenReturn(defaultDesign());
        when(gpsValidationService.validate(any(), any(), any(), any()))
                .thenReturn(new GpsValidationService.GpsValidationResult(BigDecimal.ONE, BigDecimal.valueOf(30), true));
        when(userStampRepository.save(any(UserStamp.class))).thenThrow(
                new DataIntegrityViolationException("duplicate", new RuntimeException("uq_user_stamps_collect")));

        assertThrows(StampAlreadyCollectedException.class, () -> service.collect(defaultCommand(UUID.randomUUID())));
    }

    @Test
    void collect_eventPublishFailure_stillSucceeds() {
        when(collectionPolicyService.resolveIdempotentReplay(anyString(), eq(USER_ID))).thenReturn(Optional.empty());
        when(stationScanResolverPort.resolve("NFC", "NFC1")).thenReturn(defaultStation());
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(defaultCampaign());
        when(campaignStationRepository.exists(CAMPAIGN_ID, STATION_ID)).thenReturn(true);
        when(stampDesignResolver.resolveActive(CAMPAIGN_ID, STATION_ID)).thenReturn(defaultDesign());
        when(gpsValidationService.validate(any(), any(), any(), any()))
                .thenReturn(new GpsValidationService.GpsValidationResult(BigDecimal.ONE, BigDecimal.valueOf(30), true));
        when(userStampRepository.save(any(UserStamp.class))).thenAnswer(inv -> {
            UserStamp us = inv.getArgument(0);
            us.setId(STAMP_ID);
            return us;
        });
        when(collectionQueryService.computeProgress(USER_ID, LINE_ID, CAMPAIGN_ID))
                .thenReturn(ProgressView.builder().lineId(LINE_ID).collected(1).total(10).percentage(10).build());
        doThrow(new RuntimeException("broker down")).when(eventPublisher).publishEvent(any(ApplicationEvent.class));

        assertDoesNotThrow(() -> service.collect(defaultCommand(UUID.randomUUID())));
    }

    @Test
    void collect_gpsMissing_fails() {
        when(collectionPolicyService.resolveIdempotentReplay(anyString(), eq(USER_ID))).thenReturn(Optional.empty());
        when(stationScanResolverPort.resolve("NFC", "NFC1")).thenReturn(defaultStation());
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(defaultCampaign());
        when(campaignStationRepository.exists(CAMPAIGN_ID, STATION_ID)).thenReturn(true);
        when(stampDesignResolver.resolveActive(CAMPAIGN_ID, STATION_ID)).thenReturn(defaultDesign());
        when(gpsValidationService.validate(any(), any(), any(), any()))
                .thenThrow(new GpsRequiredException("GPS latitude and longitude are required"));

        CollectStampCommand cmd = new CollectStampCommand(
                USER_ID, UUID.randomUUID(), "NFC", "NFC1", null, null, null, "ANDROID", "1.0.0", "fp");

        assertThrows(GpsRequiredException.class, () -> service.collect(cmd));
    }

    @Test
    void collect_gpsOutsideRadius_fails() {
        when(collectionPolicyService.resolveIdempotentReplay(anyString(), eq(USER_ID))).thenReturn(Optional.empty());
        when(stationScanResolverPort.resolve("NFC", "NFC1")).thenReturn(defaultStation());
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(defaultCampaign());
        when(campaignStationRepository.exists(CAMPAIGN_ID, STATION_ID)).thenReturn(true);
        when(stampDesignResolver.resolveActive(CAMPAIGN_ID, STATION_ID)).thenReturn(defaultDesign());
        when(gpsValidationService.validate(any(), any(), any(), any()))
                .thenThrow(new GpsOutOfRangeException(500, 150));

        assertThrows(GpsOutOfRangeException.class, () -> service.collect(defaultCommand(UUID.randomUUID())));
    }

    private CollectStampCommand defaultCommand(UUID idempotencyKey) {
        return new CollectStampCommand(
                USER_ID,
                idempotencyKey,
                "NFC",
                "NFC1",
                BigDecimal.valueOf(10.0),
                BigDecimal.valueOf(20.0),
                BigDecimal.valueOf(35),
                "ANDROID",
                "1.0.0",
                "device-fingerprint-123"
        );
    }

    private ResolvedStationView defaultStation() {
        return ResolvedStationView.builder()
                .id(STATION_ID)
                .lineId(LINE_ID)
                .name("Central")
                .lineName("Line 1")
                .latitude(BigDecimal.valueOf(10.0))
                .longitude(BigDecimal.valueOf(20.0))
                .zoneRadiusMeters(150)
                .scanType("NFC")
                .build();
    }

    private Campaign defaultCampaign() {
        LocalDateTime now = LocalDateTime.now(clock);
        Campaign campaign = Campaign.builder()
                .lineId(LINE_ID)
                .isDefault(true)
                .status(CampaignStatus.ACTIVE)
                .campaignType(CampaignType.STANDARD)
                .code("DEF")
                .name("C")
                .displayName("C")
                .priority(0)
                .startAt(now.minusDays(1))
                .endAt(now.plusDays(1))
                .build();
        campaign.setId(CAMPAIGN_ID);
        return campaign;
    }

    private StampDesign defaultDesign() {
        StampDesign design = StampDesign.builder()
                .campaignId(CAMPAIGN_ID)
                .stationId(STATION_ID)
                .name("S")
                .imageUrl("https://cdn/x.png")
                .status(StampDesignStatus.ACTIVE)
                .rarity(StampRarity.COMMON)
                .sortOrder(0)
                .isLimited(false)
                .build();
        design.setId(DESIGN_ID);
        return design;
    }
}
