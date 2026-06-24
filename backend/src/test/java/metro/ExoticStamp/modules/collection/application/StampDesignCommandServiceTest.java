package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.collection.application.command.CreateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.service.StampDesignCommandService;
import metro.ExoticStamp.modules.collection.application.support.CampaignAuditHelper;
import metro.ExoticStamp.modules.collection.domain.exception.DuplicateActiveStampDesignException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.service.StampDesignDomainService;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampDesignCommandServiceTest {

    @Mock private StampDesignRepository stampDesignRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignStationRepository campaignStationRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private RbacSecurityContextHelper securityContextHelper;

    private StampDesignCommandService service;
    private final UUID campaignId = UUID.randomUUID();
    private final UUID stationId = UUID.randomUUID();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new StampDesignCommandService(
                stampDesignRepository,
                campaignRepository,
                new StampDesignDomainService(campaignStationRepository),
                new CampaignAppMapper(),
                new CampaignAuditHelper(auditLogService, securityContextHelper),
                clock
        );
    }

    @Test
    void create_success() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(campaignStationRepository.exists(campaignId, stationId)).thenReturn(true);
        when(stampDesignRepository.save(any())).thenAnswer(inv -> {
            StampDesign sd = inv.getArgument(0);
            sd.setId(UUID.randomUUID());
            return sd;
        });

        var view = service.create(new CreateStampDesignCommand(
                campaignId, stationId, "Stamp", "desc", "https://img", "https://prev",
                StampRarity.RARE.name(), StampDesignStatus.DRAFT.name(), 1));

        assertEquals("Stamp", view.name());
        assertEquals("RARE", view.rarity());
    }

    @Test
    void create_stationNotInCampaign() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(campaignStationRepository.exists(campaignId, stationId)).thenReturn(false);

        assertThrows(InvalidRequestException.class, () -> service.create(new CreateStampDesignCommand(
                campaignId, stationId, "S", null, "https://img", null,
                null, StampDesignStatus.DRAFT.name(), 0)));
    }

    @Test
    void create_duplicateActive() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(campaignStationRepository.exists(campaignId, stationId)).thenReturn(true);
        when(stampDesignRepository.existsActiveByCampaignIdAndStationId(campaignId, stationId)).thenReturn(true);

        assertThrows(DuplicateActiveStampDesignException.class, () -> service.create(new CreateStampDesignCommand(
                campaignId, stationId, "S", null, "https://img", null,
                null, StampDesignStatus.ACTIVE.name(), 0)));
    }

    @Test
    void softDelete() {
        UUID id = UUID.randomUUID();
        StampDesign design = StampDesign.builder()
                .id(id).campaignId(campaignId).stationId(stationId).name("S")
                .imageUrl("https://img").rarity(StampRarity.COMMON).status(StampDesignStatus.DRAFT)
                .sortOrder(0).isLimited(false).build();
        when(stampDesignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(design));
        when(stampDesignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(id);
        verify(stampDesignRepository).save(any());
    }

    private Campaign campaign() {
        LocalDateTime now = LocalDateTime.now(clock);
        return Campaign.builder()
                .id(campaignId).code("C").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.ACTIVE)
                .startAt(now).endAt(now.plusDays(10)).priority(0).isDefault(false).build();
    }
}
