package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.service.ActiveCampaignQueryService;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveCampaignQueryServiceTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignStationRepository campaignStationRepository;
    @Mock private StampDesignRepository stampDesignRepository;
    @Mock private StationReadPort stationReadPort;
    @Mock private LineReadPort lineReadPort;

    private ActiveCampaignQueryService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);
    private final UUID campaignHighId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID campaignLowId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID stationId = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private final UUID lineId = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @BeforeEach
    void setUp() {
        service = new ActiveCampaignQueryService(
                campaignRepository,
                campaignStationRepository,
                stampDesignRepository,
                stationReadPort,
                lineReadPort,
                new CampaignAppMapper(),
                clock
        );
    }

    @Test
    void listActive_returnsActiveCampaigns() {
        Campaign active = campaign(campaignHighId, CampaignStatus.ACTIVE, 10,
                LocalDateTime.now(clock).minusDays(1), LocalDateTime.now(clock).plusDays(1));
        when(campaignRepository.findActiveInWindow(any())).thenReturn(List.of(active));
        when(campaignStationRepository.findStationIdsByCampaignId(campaignHighId)).thenReturn(List.of(stationId));
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of(
                MetroStationView.builder().id(stationId).lineId(lineId).name("S").sequence(1).active(true).build()));
        when(lineReadPort.getLineById(lineId)).thenReturn(MetroLineView.builder().id(lineId).code("L").name("L").active(true).build());
        when(stampDesignRepository.findActiveByCampaignIdAndStationIdIn(eq(campaignHighId), any())).thenReturn(List.of(
                StampDesign.builder().campaignId(campaignHighId).stationId(stationId).name("SD")
                        .imageUrl("https://img").rarity(StampRarity.COMMON).status(StampDesignStatus.ACTIVE)
                        .sortOrder(0).isLimited(false).build()));

        var result = service.listActive();
        assertEquals(1, result.size());
        assertEquals(campaignHighId, result.get(0).id());
    }

    @Test
    void listActive_priorityOrdering() {
        Campaign high = campaign(campaignHighId, CampaignStatus.ACTIVE, 10,
                LocalDateTime.now(clock).minusDays(1), LocalDateTime.now(clock).plusDays(1));
        Campaign low = campaign(campaignLowId, CampaignStatus.ACTIVE, 1,
                LocalDateTime.now(clock).minusDays(1), LocalDateTime.now(clock).plusDays(1));
        when(campaignRepository.findActiveInWindow(any())).thenReturn(List.of(high, low));
        when(campaignStationRepository.findStationIdsByCampaignId(any())).thenReturn(List.of());
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of());
        when(stampDesignRepository.findActiveByCampaignIdAndStationIdIn(any(), any())).thenReturn(List.of());

        var result = service.listActive();
        assertEquals(2, result.size());
        assertEquals(campaignHighId, result.get(0).id());
    }

    @Test
    void listActiveByStationId_emptyWhenNone() {
        when(campaignRepository.findActiveByStationId(eq(stationId), any())).thenReturn(List.of());
        assertTrue(service.listActiveByStationId(stationId).isEmpty());
    }

    @Test
    void getActiveById_notFoundWhenMissing() {
        UUID missing = UUID.randomUUID();
        when(campaignRepository.findByIdNotDeleted(missing)).thenReturn(java.util.Optional.empty());
        assertThrows(metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException.class,
                () -> service.getActiveById(missing));
    }

    @Test
    void getActiveById_notFoundWhenDraft() {
        Campaign draft = campaign(campaignHighId, CampaignStatus.DRAFT, 5,
                LocalDateTime.now(clock).minusDays(1), LocalDateTime.now(clock).plusDays(1));
        when(campaignRepository.findByIdNotDeleted(campaignHighId)).thenReturn(java.util.Optional.of(draft));
        assertThrows(metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException.class,
                () -> service.getActiveById(campaignHighId));
    }

    @Test
    void getActiveById_success() {
        Campaign active = campaign(campaignHighId, CampaignStatus.ACTIVE, 5,
                LocalDateTime.now(clock).minusDays(1), LocalDateTime.now(clock).plusDays(1));
        when(campaignRepository.findByIdNotDeleted(campaignHighId)).thenReturn(java.util.Optional.of(active));
        when(campaignStationRepository.findStationIdsByCampaignId(campaignHighId)).thenReturn(List.of());
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of());
        when(stampDesignRepository.findActiveByCampaignIdAndStationIdIn(any(), any())).thenReturn(List.of());

        var view = service.getActiveById(campaignHighId);
        assertEquals(campaignHighId, view.id());
    }

    @Test
    void listActive_skipsInactiveStationAndLine() {
        Campaign active = campaign(campaignHighId, CampaignStatus.ACTIVE, 5,
                LocalDateTime.now(clock).minusDays(1), LocalDateTime.now(clock).plusDays(1));
        when(campaignRepository.findActiveInWindow(any())).thenReturn(List.of(active));
        when(campaignStationRepository.findStationIdsByCampaignId(campaignHighId)).thenReturn(List.of(stationId));
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of(
                MetroStationView.builder().id(stationId).lineId(lineId).name("Inactive").sequence(1).active(false).build(),
                MetroStationView.builder().id(UUID.randomUUID()).lineId(lineId).name("BadLine").sequence(2).active(true).build()
        ));
        when(lineReadPort.getLineById(lineId)).thenReturn(
                MetroLineView.builder().id(lineId).code("L").name("L").active(false).build());
        when(stampDesignRepository.findActiveByCampaignIdAndStationIdIn(any(), any())).thenReturn(List.of());

        var result = service.listActive();
        assertEquals(1, result.size());
        assertTrue(result.get(0).stations().isEmpty());
    }

    private Campaign campaign(UUID id, CampaignStatus status, int priority, LocalDateTime start, LocalDateTime end) {
        Campaign c = Campaign.builder()
                .code("C-" + id.toString().substring(0, 4))
                .name("Campaign").displayName("Campaign")
                .campaignType(CampaignType.STANDARD).status(status)
                .startAt(start).endAt(end).priority(priority).isDefault(false).build();
        c.setId(id);
        return c;
    }
}
