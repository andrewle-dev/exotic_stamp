package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.common.reorder.InvalidReorderException;
import metro.ExoticStamp.common.reorder.ReorderConflictException;
import metro.ExoticStamp.common.reorder.ReorderResultView;
import metro.ExoticStamp.modules.reward.application.command.ReorderMilestonesCommand;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneCommandServiceTest {

    @Mock private MilestoneRepository milestoneRepository;
    private RewardAppMapper rewardAppMapper;
    @Mock private RewardAuditHelper rewardAuditHelper;

    private MilestoneCommandService service;
    private final UUID campaignId = UUID.randomUUID();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        rewardAppMapper = new RewardAppMapper();
        service = new MilestoneCommandService(milestoneRepository, rewardAppMapper, rewardAuditHelper, clock);
    }

    @Test
    void reorder_denseRenumbers() {
        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        Milestone a = milestone(aId, 3);
        Milestone b = milestone(bId, 1);
        when(milestoneRepository.findAllByCampaignIdOrderBySortOrderAsc(campaignId)).thenReturn(List.of(a, b));
        when(milestoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReorderResultView result = service.reorder(new ReorderMilestonesCommand(campaignId, List.of(bId, aId)));

        assertEquals(campaignId, result.scopeId());
        assertEquals(2, result.updatedCount());
        assertEquals(0, b.getSortOrder());
        assertEquals(1, a.getSortOrder());
        verify(milestoneRepository, times(2)).save(any());
    }

    @Test
    void reorder_incompleteSet_throwsConflict() {
        UUID aId = UUID.randomUUID();
        when(milestoneRepository.findAllByCampaignIdOrderBySortOrderAsc(campaignId))
                .thenReturn(List.of(milestone(aId, 0), milestone(UUID.randomUUID(), 1)));

        assertThrows(ReorderConflictException.class, () ->
                service.reorder(new ReorderMilestonesCommand(campaignId, List.of(aId))));
    }

    @Test
    void reorder_duplicateIds_throwsInvalid() {
        UUID aId = UUID.randomUUID();
        assertThrows(InvalidReorderException.class, () ->
                service.reorder(new ReorderMilestonesCommand(campaignId, List.of(aId, aId))));
    }

    @Test
    void reorder_archivedMilestone_throwsInvalid() {
        UUID aId = UUID.randomUUID();
        Milestone archived = milestone(aId, 0);
        archived.setStatus(MilestoneStatus.ARCHIVED);
        when(milestoneRepository.findAllByCampaignIdOrderBySortOrderAsc(campaignId))
                .thenReturn(List.of(archived));

        assertThrows(InvalidReorderException.class, () ->
                service.reorder(new ReorderMilestonesCommand(campaignId, List.of(aId))));
    }

    @Test
    void create_success() {
        when(milestoneRepository.existsByCampaignIdAndCodeAndIdNot(campaignId, "M1", null)).thenReturn(false);
        when(milestoneRepository.save(any())).thenAnswer(inv -> {
            Milestone m = inv.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        var view = service.create(new metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand(
                campaignId, "M1", 3, "Three", "desc", "VOUCHER", "Voucher", null, null, "DRAFT", 0));

        assertEquals("M1", view.code());
        verify(rewardAuditHelper).scheduleMilestoneCreated(any());
    }

    @Test
    void create_duplicateCode_throws() {
        when(milestoneRepository.existsByCampaignIdAndCodeAndIdNot(campaignId, "M1", null)).thenReturn(true);

        assertThrows(metro.ExoticStamp.modules.reward.domain.exception.MilestoneCodeDuplicateException.class, () ->
                service.create(new metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand(
                        campaignId, "M1", 3, "Three", null, "VOUCHER", "Voucher", null, null, null, 0)));
    }

    @Test
    void softDelete_setsInactiveAndDeletedAt() {
        UUID id = UUID.randomUUID();
        Milestone m = milestone(id, 0);
        when(milestoneRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(m));
        when(milestoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(id);

        assertEquals(MilestoneStatus.INACTIVE, m.getStatus());
        assertTrue(m.getDeletedAt() != null);
        verify(rewardAuditHelper).scheduleMilestoneDisabled(id);
    }

    @Test
    void update_archivedMilestone_rejected() {
        UUID id = UUID.randomUUID();
        Milestone m = milestone(id, 0);
        m.setStatus(MilestoneStatus.ARCHIVED);
        when(milestoneRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(m));

        assertThrows(metro.ExoticStamp.modules.reward.domain.exception.MilestoneArchivedException.class, () ->
                service.update(new metro.ExoticStamp.modules.reward.application.command.UpdateMilestoneCommand(
                        id, "NEW", null, null, null, null, null, null, null, null, null)));
    }

    @Test
    void create_missingCampaignId_rejected() {
        assertThrows(metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException.class, () ->
                service.create(new metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand(
                        null, "M1", 3, "Three", null, "VOUCHER", "Voucher", null, null, null, 0)));
    }

    @Test
    void create_missingRewardType_rejected() {
        assertThrows(metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException.class, () ->
                service.create(new metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand(
                        campaignId, "M1", 3, "Three", null, null, "Voucher", null, null, null, 0)));
    }

    @Test
    void update_success() {
        UUID id = UUID.randomUUID();
        Milestone m = milestone(id, 0);
        when(milestoneRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(m));
        when(milestoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.update(new metro.ExoticStamp.modules.reward.application.command.UpdateMilestoneCommand(
                id, "NEW", 5, "Updated", "desc", "DIGITAL_STICKER", "Sticker", null, null, "ACTIVE", 1));

        assertEquals("NEW", view.code());
        verify(rewardAuditHelper).scheduleMilestoneUpdated(id);
    }

    @Test
    void update_duplicateCode_throws() {
        UUID id = UUID.randomUUID();
        Milestone m = milestone(id, 0);
        when(milestoneRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(m));
        when(milestoneRepository.existsByCampaignIdAndCodeAndIdNot(campaignId, "DUP", id)).thenReturn(true);

        assertThrows(metro.ExoticStamp.modules.reward.domain.exception.MilestoneCodeDuplicateException.class, () ->
                service.update(new metro.ExoticStamp.modules.reward.application.command.UpdateMilestoneCommand(
                        id, "DUP", null, null, null, null, null, null, null, null, null)));
    }

    @Test
    void reorder_nullCampaignId_throws() {
        assertThrows(InvalidReorderException.class, () ->
                service.reorder(new ReorderMilestonesCommand(null, List.of(UUID.randomUUID()))));
    }

    private Milestone milestone(UUID id, int sortOrder) {
        return Milestone.builder()
                .id(id)
                .campaignId(campaignId)
                .code("M-" + sortOrder)
                .stampsRequired(sortOrder + 1)
                .name("Milestone " + sortOrder)
                .rewardType(RewardType.DIGITAL_STICKER)
                .rewardTitle("Reward")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(sortOrder)
                .active(true)
                .build();
    }
}
