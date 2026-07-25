package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.collection.application.service.CampaignStationQueryService;
import metro.ExoticStamp.modules.collection.application.view.CampaignStationView;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignStationQueryServiceTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignStationRepository campaignStationRepository;
    @Mock private StationReadPort stationReadPort;

    private CampaignStationQueryService service;
    private final UUID campaignId = UUID.randomUUID();
    private final UUID stationA = UUID.randomUUID();
    private final UUID stationB = UUID.randomUUID();
    private final UUID lineId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CampaignStationQueryService(campaignRepository, campaignStationRepository, stationReadPort);
    }

    @Test
    void listByCampaignId_campaignMissing_throws() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.empty());
        assertThrows(CampaignNotFoundException.class, () -> service.listByCampaignId(campaignId));
    }

    @Test
    void listByCampaignId_noStations_returnsEmpty() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(campaignStationRepository.findStationIdsByCampaignId(campaignId)).thenReturn(List.of());

        assertTrue(service.listByCampaignId(campaignId).isEmpty());
        verify(stationReadPort, never()).listStationViewsByIds(any());
    }

    @Test
    void listByCampaignId_skipsMissingStationViews() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(campaignStationRepository.findStationIdsByCampaignId(campaignId)).thenReturn(List.of(stationA, stationB));
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of(
                MetroStationView.builder().id(stationA).lineId(lineId).name("Alpha").sequence(2).active(true).build()
        ));

        List<CampaignStationView> result = service.listByCampaignId(campaignId);

        assertEquals(1, result.size());
        assertEquals(stationA, result.get(0).stationId());
        assertEquals("Alpha", result.get(0).name());
        assertEquals(2, result.get(0).sortOrder());
    }

    @Test
    void listByCampaignId_nullSequence_defaultsSortOrderZero() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(campaignStationRepository.findStationIdsByCampaignId(campaignId)).thenReturn(List.of(stationA));
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of(
                MetroStationView.builder().id(stationA).lineId(lineId).name("NoSeq").sequence(null).active(true).build()
        ));

        List<CampaignStationView> result = service.listByCampaignId(campaignId);

        assertEquals(0, result.get(0).sortOrder());
    }

    @Test
    void listByCampaignId_preservesStationOrder() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(campaignStationRepository.findStationIdsByCampaignId(campaignId)).thenReturn(List.of(stationA, stationB));
        when(stationReadPort.listStationViewsByIds(any())).thenReturn(List.of(
                MetroStationView.builder().id(stationA).lineId(lineId).name("Alpha").sequence(1).active(true).build(),
                MetroStationView.builder().id(stationB).lineId(lineId).name("Beta").sequence(2).active(true).build()
        ));

        List<CampaignStationView> result = service.listByCampaignId(campaignId);

        assertEquals(2, result.size());
        assertEquals(stationA, result.get(0).stationId());
        assertEquals(stationB, result.get(1).stationId());
        assertEquals("Beta", result.get(1).name());
    }

    private Campaign campaign() {
        Campaign c = Campaign.builder()
                .code("C1")
                .name("Campaign")
                .displayName("Campaign")
                .campaignType(CampaignType.STANDARD)
                .status(CampaignStatus.ACTIVE)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(30))
                .priority(0)
                .isDefault(false)
                .build();
        c.setId(campaignId);
        return c;
    }
}