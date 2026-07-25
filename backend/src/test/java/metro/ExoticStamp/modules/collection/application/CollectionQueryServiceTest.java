package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.application.support.DefaultCampaignResolver;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.application.view.CollectOutcomeStatus;
import metro.ExoticStamp.modules.collection.application.view.CollectStatusView;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.model.UserStampSlice;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionQueryServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID STATION_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID CAMPAIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID DESIGN_ID = UUID.fromString("00000000-0000-0000-0000-000000000030");

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignStationRepository campaignStationRepository;
    @Mock private DefaultCampaignResolver defaultCampaignResolver;
    @Mock private StampDesignRepository stampDesignRepository;
    @Mock private UserStampRepository userStampRepository;
    @Mock private StationReadPort stationReadPort;
    @Mock private LineReadPort lineReadPort;
    @Mock private UserStampCachePort cachePort;
    @Mock private CollectionProperties collectionProperties;

    private Clock clock;
    private CollectionQueryService service;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-06-24T12:00:00Z"), ZoneOffset.UTC);
        lenient().when(collectionProperties.getDefaultPageSize()).thenReturn(20);
        lenient().when(collectionProperties.getMaxPageSize()).thenReturn(50);
        lenient().when(collectionProperties.getIdempotencyWindow()).thenReturn(Duration.ofHours(1));
        service = new CollectionQueryService(
                campaignRepository,
                campaignStationRepository,
                defaultCampaignResolver,
                stampDesignRepository,
                userStampRepository,
                stationReadPort,
                lineReadPort,
                cachePort,
                new UserStampAppMapper(),
                collectionProperties,
                clock
        );
    }

    @Test
    void progressQuery_usesActiveStampDesignCount() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(c);
        when(cachePort.getUserProgress(USER_ID, LINE_ID)).thenReturn(Optional.empty());
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(3L);
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(8L);

        ProgressView res = service.getMyProgress(USER_ID, LINE_ID, null);
        assertEquals(3L, res.collected());
        assertEquals(8L, res.total());
        assertEquals(37, res.percentage());
        verify(cachePort).putUserProgress(eq(USER_ID), eq(LINE_ID), any());
    }

    @Test
    void progressQuery_fullCollection_returnsCollectedAndTotalFourteen() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(c);
        when(cachePort.getUserProgress(USER_ID, LINE_ID)).thenReturn(Optional.empty());
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(14L);
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(14L);

        ProgressView res = service.getMyProgress(USER_ID, LINE_ID, null);
        assertEquals(14L, res.collected());
        assertEquals(14L, res.total());
        assertEquals(100, res.percentage());
    }

    @Test
    void progressQuery_noStampsButDesignsAvailable_returnsZeroOfTotal() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(c);
        when(cachePort.getUserProgress(USER_ID, LINE_ID)).thenReturn(Optional.empty());
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(0L);
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(14L);

        ProgressView res = service.getMyProgress(USER_ID, LINE_ID, null);
        assertEquals(0L, res.collected());
        assertEquals(14L, res.total());
        assertEquals(0, res.percentage());
    }

    @Test
    void progressQuery_inactiveDesignsExcludedFromTotal() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(c);
        when(cachePort.getUserProgress(USER_ID, LINE_ID)).thenReturn(Optional.empty());
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(2L);
        // Repository countActive only returns ACTIVE designs — stub the filtered total.
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(10L);

        ProgressView res = service.getMyProgress(USER_ID, LINE_ID, null);
        assertEquals(2L, res.collected());
        assertEquals(10L, res.total());
        verify(stampDesignRepository).countActiveByCampaignId(CAMPAIGN_ID);
    }

    @Test
    void progressQuery_collectedExceedsActiveTotal_capsPercentageAt100() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(c);
        when(cachePort.getUserProgress(USER_ID, LINE_ID)).thenReturn(Optional.empty());
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(5L);
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(3L);

        ProgressView res = service.getMyProgress(USER_ID, LINE_ID, null);
        assertEquals(5L, res.collected());
        assertEquals(3L, res.total());
        assertEquals(100, res.percentage());
    }

    @Test
    void stampBookQuery_usesCampaignStations() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(c);
        when(cachePort.getStampBook(USER_ID, LINE_ID, CAMPAIGN_ID)).thenReturn(Optional.empty());
        when(campaignStationRepository.findStationIdsByCampaignId(CAMPAIGN_ID))
                .thenReturn(List.of(STATION_ID, STATION_ID_2));
        when(stationReadPort.listStationViewsByIds(Set.of(STATION_ID, STATION_ID_2))).thenReturn(List.of(
                MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build(),
                MetroStationView.builder().id(STATION_ID_2).lineId(LINE_ID).name("Next").sequence(2).active(true).build()
        ));
        when(lineReadPort.getLineById(LINE_ID))
                .thenReturn(MetroLineView.builder().id(LINE_ID).code("L1").name("Line 1").active(true).build());
        when(userStampRepository.findByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(List.of(
                UserStamp.builder().userId(USER_ID).stationId(STATION_ID).campaignId(CAMPAIGN_ID).stampDesignId(DESIGN_ID)
                        .collectedAt(LocalDateTime.now()).gpsVerified(true).collectMethod(CollectMethod.NFC)
                        .deviceFingerprint("fp").idempotencyKey("k").collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN").build()
        ));
        when(stampDesignRepository.findActiveByCampaignIdAndStationIdIn(eq(CAMPAIGN_ID), anyList()))
                .thenReturn(List.of(StampDesign.builder().stationId(STATION_ID).campaignId(CAMPAIGN_ID).name("S")
                        .imageUrl("https://cdn/central.png").status(StampDesignStatus.ACTIVE).rarity(StampRarity.COMMON)
                        .sortOrder(0).isLimited(false).build()));
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(2L);
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(1L);

        StampBookView res = service.getStampBook(USER_ID, LINE_ID);
        assertEquals(2, res.stations().size());
        assertTrue(res.stations().get(0).collected());
        assertFalse(res.stations().get(1).collected());
        assertEquals("Line 1", res.lineName());
        assertNotNull(res.progress());
        verify(cachePort).putStampBook(eq(USER_ID), eq(LINE_ID), eq(CAMPAIGN_ID), any());
    }

    @Test
    void stampBook_withoutLineId_usesGlobalDefault() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(null)).thenReturn(c);
        when(cachePort.getStampBook(USER_ID, LINE_ID, CAMPAIGN_ID)).thenReturn(Optional.empty());
        when(campaignStationRepository.findStationIdsByCampaignId(CAMPAIGN_ID)).thenReturn(List.of());
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of());
        when(lineReadPort.getLineById(LINE_ID))
                .thenReturn(MetroLineView.builder().id(LINE_ID).code("L1").name("Line 1").active(true).build());
        when(userStampRepository.findByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(List.of());
        when(stampDesignRepository.findActiveByCampaignIdAndStationIdIn(eq(CAMPAIGN_ID), anyList())).thenReturn(List.of());
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(0L);
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(0L);

        StampBookView res = service.getStampBook(USER_ID, null);
        assertEquals(CAMPAIGN_ID, res.campaignId());
        verify(defaultCampaignResolver).resolveActiveGlobalDefault(null);
    }

    @Test
    void historyQuery_returnsRecentPage() {
        UserStamp us = UserStamp.builder()
                .userId(USER_ID)
                .stationId(STATION_ID)
                .campaignId(CAMPAIGN_ID)
                .stampDesignId(DESIGN_ID)
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.QR)
                .deviceFingerprint("device-fingerprint-123")
                .idempotencyKey(UUID.randomUUID().toString())
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN")
                .build();
        when(cachePort.getUserHistory(USER_ID, 0, 20)).thenReturn(Optional.empty());
        when(userStampRepository.findByUserIdPaged(USER_ID, 0, 20))
                .thenReturn(new UserStampSlice(List.of(us), 1, 1, 0, 20));
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of(
                MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build()));
        StampDesign sd = StampDesign.builder().name("S").imageUrl("https://cdn/x.png")
                .status(StampDesignStatus.ACTIVE).rarity(StampRarity.COMMON).sortOrder(0).isLimited(false).build();
        sd.setId(DESIGN_ID);
        when(stampDesignRepository.findAllByIdIn(any())).thenReturn(List.of(sd));

        PageResponse<UserStampView> res = service.getMyHistory(USER_ID, 0, 20);
        assertEquals(1, res.content().size());
        assertEquals("Central", res.content().get(0).stationName());
        verify(cachePort).putUserHistory(eq(USER_ID), eq(0), eq(20), any());
    }

    @Test
    void collectStatus_missingUserId_throws() {
        assertThrows(InvalidRequestException.class,
                () -> service.getCollectStatus(null, UUID.randomUUID()));
    }

    @Test
    void collectStatus_missingIdempotencyKey_throws() {
        assertThrows(InvalidRequestException.class,
                () -> service.getCollectStatus(USER_ID, null));
    }

    @Test
    void collectStatus_notFound_whenNoStampForKey() {
        UUID key = UUID.fromString("550e8400-e29b-41d4-a716-446655440099");
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(USER_ID, key.toString()))
                .thenReturn(Optional.empty());

        CollectStatusView res = service.getCollectStatus(USER_ID, key);
        assertEquals(CollectOutcomeStatus.NOT_FOUND, res.status());
        assertNull(res.stamp());
    }

    @Test
    void collectStatus_success_whenStampOutsideIdempotencyWindow() {
        UUID key = UUID.fromString("550e8400-e29b-41d4-a716-446655440099");
        LocalDateTime collectedAt = LocalDateTime.now(clock).minusHours(2);
        UserStamp stamp = UserStamp.builder()
                .userId(USER_ID)
                .stationId(STATION_ID)
                .campaignId(CAMPAIGN_ID)
                .stampDesignId(DESIGN_ID)
                .collectedAt(collectedAt)
                .createdAt(collectedAt)
                .gpsVerified(true)
                .collectMethod(CollectMethod.NFC)
                .deviceFingerprint("fp")
                .idempotencyKey(key.toString())
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN")
                .sourceScanType("NFC")
                .build();
        stamp.setId(UUID.randomUUID());

        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(USER_ID, key.toString()))
                .thenReturn(Optional.of(stamp));
        when(stationReadPort.getStationViewById(STATION_ID)).thenReturn(
                MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());
        when(lineReadPort.getLineById(LINE_ID))
                .thenReturn(MetroLineView.builder().id(LINE_ID).code("M1").name("Line 1").active(true).build());
        when(stampDesignRepository.findById(DESIGN_ID)).thenReturn(Optional.of(
                StampDesign.builder().imageUrl("/stamp.png").status(StampDesignStatus.ACTIVE)
                        .rarity(StampRarity.COMMON).sortOrder(0).name("S").isLimited(false).build()));
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(1L);
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(5L);

        CollectStatusView res = service.getCollectStatus(USER_ID, key);
        assertEquals(CollectOutcomeStatus.SUCCESS, res.status());
        assertNotNull(res.stamp());
        assertEquals(STATION_ID, res.stamp().stationId());
        assertEquals(collectedAt, res.resolvedAt());
    }

    @Test
    void collectStatus_duplicate_whenStampWithinIdempotencyWindow() {
        UUID key = UUID.fromString("550e8400-e29b-41d4-a716-446655440088");
        LocalDateTime collectedAt = LocalDateTime.now(clock).minusMinutes(10);
        UserStamp stamp = UserStamp.builder()
                .userId(USER_ID)
                .stationId(STATION_ID)
                .campaignId(CAMPAIGN_ID)
                .stampDesignId(DESIGN_ID)
                .collectedAt(collectedAt)
                .createdAt(collectedAt)
                .gpsVerified(true)
                .collectMethod(CollectMethod.NFC)
                .deviceFingerprint("fp")
                .idempotencyKey(key.toString())
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN")
                .sourceScanType("NFC")
                .build();
        stamp.setId(UUID.randomUUID());

        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(USER_ID, key.toString()))
                .thenReturn(Optional.of(stamp));
        when(stationReadPort.getStationViewById(STATION_ID)).thenReturn(
                MetroStationView.builder().id(STATION_ID).lineId(LINE_ID).name("Central").sequence(1).active(true).build());
        when(lineReadPort.getLineById(LINE_ID))
                .thenReturn(MetroLineView.builder().id(LINE_ID).code("M1").name("Line 1").active(true).build());
        when(stampDesignRepository.findById(DESIGN_ID)).thenReturn(Optional.empty());
        when(userStampRepository.countDistinctStationsByUserIdAndCampaignId(USER_ID, CAMPAIGN_ID)).thenReturn(1L);
        when(stampDesignRepository.countActiveByCampaignId(CAMPAIGN_ID)).thenReturn(5L);

        CollectStatusView res = service.getCollectStatus(USER_ID, key);
        assertEquals(CollectOutcomeStatus.DUPLICATE, res.status());
        assertNotNull(res.stamp());
    }

    @Test
    void progressQuery_cacheHit_skipsRepository() {
        Campaign c = defaultCampaign();
        when(defaultCampaignResolver.resolveActiveGlobalDefault(LINE_ID)).thenReturn(c);
        ProgressView cached = ProgressView.builder()
                .lineId(LINE_ID)
                .collected(7)
                .total(14)
                .percentage(50)
                .build();
        when(cachePort.getUserProgress(USER_ID, LINE_ID)).thenReturn(Optional.of(cached));

        ProgressView res = service.getMyProgress(USER_ID, LINE_ID, null);

        assertEquals(7L, res.collected());
        assertEquals(50, res.percentage());
    }

    private Campaign defaultCampaign() {
        Campaign c = Campaign.builder().lineId(LINE_ID).isDefault(true).status(CampaignStatus.ACTIVE)
                .campaignType(CampaignType.STANDARD).code("DEF").name("C").displayName("Campaign C").priority(0)
                .startAt(LocalDateTime.now()).endAt(LocalDateTime.now().plusDays(1)).build();
        c.setId(CAMPAIGN_ID);
        return c;
    }
}
