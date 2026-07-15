package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.common.reorder.InvalidReorderException;
import metro.ExoticStamp.common.reorder.ReorderConflictException;
import metro.ExoticStamp.common.reorder.ReorderResultView;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.collection.application.command.CreateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.command.ReorderStampDesignsCommand;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.service.CampaignStationCommandService;
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
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampDesignCommandServiceTest {

    @Mock private StampDesignRepository stampDesignRepository;
    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignStationCommandService campaignStationCommandService;
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
                campaignStationCommandService,
                new CampaignAppMapper(),
                new CampaignAuditHelper(auditLogService, securityContextHelper),
                clock
        );
    }

    @Test
    void create_success() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(stampDesignRepository.save(any())).thenAnswer(inv -> {
            StampDesign sd = inv.getArgument(0);
            sd.setId(UUID.randomUUID());
            return sd;
        });

        var view = service.create(new CreateStampDesignCommand(
                campaignId, stationId, "Stamp", "desc", "https://img", "https://prev",
                StampRarity.RARE.name(), StampDesignStatus.DRAFT.name(), 1));

        assertEquals("Stamp", view.name());
        verify(campaignStationCommandService).ensureAssigned(campaignId, stationId);
        verify(stampDesignRepository).save(any());
    }

    @Test
    void create_duplicateActive_throws() {
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(stampDesignRepository.existsActiveByCampaignIdAndStationId(campaignId, stationId)).thenReturn(true);

        assertThrows(DuplicateActiveStampDesignException.class, () ->
                service.create(new CreateStampDesignCommand(
                        campaignId, stationId, "Stamp", null, "https://img", null,
                        StampRarity.COMMON.name(), StampDesignStatus.ACTIVE.name(), 0)));
        verify(campaignStationCommandService).ensureAssigned(campaignId, stationId);
    }

    @Test
    void softDelete_setsDeletedAt() {
        UUID id = UUID.randomUUID();
        StampDesign sd = StampDesign.builder()
                .id(id).campaignId(campaignId).stationId(stationId).name("S")
                .imageUrl("https://img").rarity(StampRarity.COMMON).status(StampDesignStatus.DRAFT)
                .sortOrder(0).isLimited(false).createdAt(LocalDateTime.now(clock)).build();
        when(stampDesignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(sd));
        when(stampDesignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(id);
        verify(stampDesignRepository).save(any());
    }

    @Test
    void softDelete_missing_throws() {
        UUID id = UUID.randomUUID();
        when(stampDesignRepository.findByIdNotDeleted(id)).thenReturn(Optional.empty());
        assertThrows(InvalidRequestException.class, () -> service.softDelete(id));
    }

    @Test
    void reorder_denseRenumbers() {
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        StampDesign a = design(aId, 5);
        StampDesign b = design(bId, 2);
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(stampDesignRepository.findByCampaignIdOrderBySortOrderAsc(campaignId)).thenReturn(List.of(a, b));
        when(stampDesignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReorderResultView result = service.reorder(new ReorderStampDesignsCommand(campaignId, List.of(bId, aId)));

        assertEquals(campaignId, result.scopeId());
        assertEquals(2, result.updatedCount());
        assertEquals(0, b.getSortOrder());
        assertEquals(1, a.getSortOrder());
        verify(stampDesignRepository, times(2)).save(any());
    }

    @Test
    void reorder_incompleteSet_throwsConflict() {
        UUID aId = UUID.randomUUID();
        StampDesign a = design(aId, 0);
        StampDesign b = design(UUID.randomUUID(), 1);
        when(campaignRepository.findByIdNotDeleted(campaignId)).thenReturn(Optional.of(campaign()));
        when(stampDesignRepository.findByCampaignIdOrderBySortOrderAsc(campaignId)).thenReturn(List.of(a, b));

        assertThrows(ReorderConflictException.class, () ->
                service.reorder(new ReorderStampDesignsCommand(campaignId, List.of(aId))));
    }

    @Test
    void reorder_duplicateIds_throwsInvalid() {
        UUID aId = UUID.randomUUID();
        assertThrows(InvalidReorderException.class, () ->
                service.reorder(new ReorderStampDesignsCommand(campaignId, List.of(aId, aId))));
    }

    private StampDesign design(UUID id, int sortOrder) {
        return StampDesign.builder()
                .id(id).campaignId(campaignId).stationId(stationId).name("S-" + sortOrder)
                .imageUrl("https://img").rarity(StampRarity.COMMON).status(StampDesignStatus.ACTIVE)
                .sortOrder(sortOrder).isLimited(false).createdAt(LocalDateTime.now(clock)).build();
    }

    private Campaign campaign() {
        return Campaign.builder()
                .id(campaignId).code("C1").name("Campaign").campaignType(CampaignType.STANDARD)
                .status(CampaignStatus.ACTIVE).priority(0).createdAt(LocalDateTime.now(clock)).build();
    }
}
