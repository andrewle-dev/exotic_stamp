package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MilestoneQueryServiceTest {

    @Mock private MilestoneRepository milestoneRepository;

    private MilestoneQueryService service;
    private final UUID campaignId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RewardProperties props = new RewardProperties();
        props.setDefaultPageSize(20);
        props.setMaxPageSize(50);
        service = new MilestoneQueryService(milestoneRepository, new RewardAppMapper(), props);
    }

    @Test
    void list_clampsNegativePageAndOversizedRequest() {
        Milestone m = sampleMilestone();
        when(milestoneRepository.findAllNotDeletedPaged(
                eq(campaignId), eq(MilestoneStatus.ACTIVE), eq(0), eq(50)))
                .thenReturn(new PagedSlice<>(List.of(m), 1, 1, 0, 50));

        PageResponse<MilestoneView> page = service.list(campaignId, "ACTIVE", -5, 100);

        assertEquals(1, page.content().size());
        assertEquals("M1", page.content().get(0).code());
    }

    @Test
    void list_nullStatus_returnsAllNonDeleted() {
        when(milestoneRepository.findAllNotDeletedPaged(
                eq(campaignId), isNull(), eq(0), eq(20)))
                .thenReturn(new PagedSlice<>(List.of(), 0, 0, 0, 20));

        service.list(campaignId, "  ", 0, 0);

        verify(milestoneRepository).findAllNotDeletedPaged(campaignId, null, 0, 20);
    }

    @Test
    void get_found() {
        UUID id = UUID.randomUUID();
        Milestone m = sampleMilestone();
        m.setId(id);
        when(milestoneRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(m));

        MilestoneView view = service.get(id);

        assertEquals(id, view.id());
        assertEquals(RewardType.DIGITAL_STICKER, view.rewardType());
    }

    @Test
    void get_notFound() {
        UUID id = UUID.randomUUID();
        when(milestoneRepository.findByIdNotDeleted(id)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class, () -> service.get(id));
    }

    private Milestone sampleMilestone() {
        return Milestone.builder()
                .campaignId(campaignId)
                .code("M1")
                .stampsRequired(3)
                .name("Three stamps")
                .rewardType(RewardType.DIGITAL_STICKER)
                .rewardTitle("Sticker")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .build();
    }
}
