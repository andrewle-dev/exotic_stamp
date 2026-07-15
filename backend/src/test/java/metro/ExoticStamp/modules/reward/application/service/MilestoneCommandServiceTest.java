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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneCommandServiceTest {

    @Mock private MilestoneRepository milestoneRepository;
    @Mock private RewardAppMapper rewardAppMapper;
    @Mock private RewardAuditHelper rewardAuditHelper;

    private MilestoneCommandService service;
    private final UUID campaignId = UUID.randomUUID();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
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
    void reorder_nullCampaign_throwsInvalid() {
        assertThrows(InvalidReorderException.class, () ->
                service.reorder(new ReorderMilestonesCommand(null, List.of())));
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
