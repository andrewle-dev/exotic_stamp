package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.collection.application.service.CampaignStationCommandService;
import metro.ExoticStamp.modules.collection.application.support.CampaignAuditHelper;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignStationDuplicateException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidStationException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.service.CampaignDomainService;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignStationCommandServiceTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignStationRepository campaignStationRepository;
    @Mock private StationReadPort stationReadPort;
    @Mock private LineReadPort lineReadPort;
    @Mock private AuditLogService auditLogService;
    @Mock private RbacSecurityContextHelper securityContextHelper;

    private CampaignStationCommandService service;

    @BeforeEach
    void setUp() {
        service = new CampaignStationCommandService(
                campaignRepository,
                campaignStationRepository,
                new CampaignDomainService(),
                stationReadPort,
                lineReadPort,
                new CampaignAuditHelper(auditLogService, securityContextHelper)
        );
    }

    @Test
    void assign_success() {
        UUID campaignId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(activeCampaign(campaignId)));
        when(stationReadPort.getStationViewById(stationId)).thenReturn(MetroStationView.builder()
                .id(stationId).lineId(lineId).name("S").sequence(1).active(true).build());
        when(lineReadPort.getLineById(lineId)).thenReturn(MetroLineView.builder().id(lineId).code("L1").name("L").active(true).build());

        service.assign(campaignId, stationId);
        verify(campaignStationRepository).assign(campaignId, stationId);
    }

    @Test
    void assign_duplicate() {
        UUID campaignId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(activeCampaign(campaignId)));
        when(stationReadPort.getStationViewById(stationId)).thenReturn(MetroStationView.builder()
                .id(stationId).lineId(UUID.randomUUID()).name("S").sequence(1).active(true).build());
        when(lineReadPort.getLineById(any())).thenReturn(MetroLineView.builder().id(UUID.randomUUID()).code("L").name("L").active(true).build());

        doThrow(new CampaignStationDuplicateException()).when(campaignStationRepository).assign(campaignId, stationId);

        assertThrows(CampaignStationDuplicateException.class, () -> service.assign(campaignId, stationId));
    }

    @Test
    void assign_inactiveStation() {
        UUID campaignId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(activeCampaign(campaignId)));
        when(stationReadPort.getStationViewById(stationId)).thenReturn(MetroStationView.builder()
                .id(stationId).lineId(UUID.randomUUID()).name("S").sequence(1).active(false).build());

        assertThrows(InvalidStationException.class, () -> service.assign(campaignId, stationId));
    }

    @Test
    void remove_assignment() {
        UUID campaignId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(activeCampaign(campaignId)));

        service.remove(campaignId, stationId);
        verify(campaignStationRepository).remove(campaignId, stationId);
    }

    private static Campaign activeCampaign(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return Campaign.builder()
                .id(id).code("C").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(now).endAt(now.plusDays(10)).priority(0).isDefault(false).build();
    }
}
