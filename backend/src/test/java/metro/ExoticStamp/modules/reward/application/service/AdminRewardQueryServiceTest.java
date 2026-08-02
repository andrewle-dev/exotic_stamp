package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import metro.ExoticStamp.modules.reward.domain.repository.RewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRewardQueryServiceTest {

    @Mock private PartnerRepository partnerRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private RewardRepository rewardRepository;
    @Mock private VoucherPoolRepository voucherPoolRepository;

    private AdminRewardQueryService service;

    @BeforeEach
    void setUp() {
        RewardProperties properties = new RewardProperties();
        properties.setDefaultPageSize(20);
        properties.setMaxPageSize(50);
        service = new AdminRewardQueryService(
                partnerRepository,
                milestoneRepository,
                rewardRepository,
                voucherPoolRepository,
                new RewardAppMapper(),
                properties
        );
    }

    @Test
    void listRewards_mapsPagedResults() {
        UUID rewardId = UUID.randomUUID();
        Reward reward = Reward.builder()
                .id(rewardId)
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("Voucher")
                .issuedCount(1)
                .active(true)
                .build();
        when(rewardRepository.findAllPaged(null, 0, 20))
                .thenReturn(new PagedSlice<>(List.of(reward), 1, 1, 0, 20));

        var page = service.listRewards(null, 0, 0);

        assertEquals(1, page.content().size());
        assertEquals(rewardId, page.content().get(0).id());
        assertEquals("Voucher", page.content().get(0).name());
        verify(rewardRepository).findAllPaged(null, 0, 20);
    }

    @Test
    void listRewards_clampsPageSizeToMax() {
        when(rewardRepository.findAllPaged(eq(true), eq(1), eq(50)))
                .thenReturn(new PagedSlice<>(List.of(), 0, 0, 1, 50));

        service.listRewards(true, 1, 999);

        verify(rewardRepository).findAllPaged(true, 1, 50);
    }

    @Test
    void getReward_notFoundThrows() {
        UUID id = UUID.randomUUID();
        when(rewardRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RewardNotFoundException.class, () -> service.getReward(id));
    }

    @Test
    void getPartner_notFoundThrows() {
        UUID id = UUID.randomUUID();
        when(partnerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PartnerNotFoundException.class, () -> service.getPartner(id));
    }

    @Test
    void getMilestone_notFoundThrows() {
        UUID id = UUID.randomUUID();
        when(milestoneRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class, () -> service.getMilestone(id));
    }

    @Test
    void getVoucherStats_whenRewardExists() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(true);
        when(voucherPoolRepository.countAvailableByRewardId(rewardId)).thenReturn(7L);
        when(voucherPoolRepository.countRedeemedByRewardId(rewardId)).thenReturn(3L);

        var stats = service.getVoucherStats(rewardId);

        assertEquals(7L, stats.availableCount());
        assertEquals(3L, stats.redeemedCount());
    }

    @Test
    void getVoucherStats_whenRewardMissing() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(false);

        assertThrows(RewardNotFoundException.class, () -> service.getVoucherStats(rewardId));
    }

    @Test
    void listPartners_mapsActiveFlag() {
        UUID partnerId = UUID.randomUUID();
        Partner partner = Partner.builder()
                .id(partnerId)
                .name("Metro Coffee")
                .active(true)
                .build();
        when(partnerRepository.findAllPaged(true, 0, 20))
                .thenReturn(new PagedSlice<>(List.of(partner), 1, 1, 0, 20));

        var page = service.listPartners(true, 0, 0);

        assertEquals(partnerId, page.content().get(0).id());
        assertEquals("Metro Coffee", page.content().get(0).name());
    }

    @Test
    void listMilestones_mapsRequiredStampCount() {
        UUID milestoneId = UUID.randomUUID();
        Milestone milestone = Milestone.builder()
                .id(milestoneId)
                .campaignId(UUID.randomUUID())
                .code("M5")
                .stampsRequired(5)
                .name("Five stamps")
                .rewardType(RewardType.DIGITAL_BADGE)
                .rewardTitle("Badge")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(1)
                .build();
        when(milestoneRepository.findAllPaged(null, 0, 20))
                .thenReturn(new PagedSlice<>(List.of(milestone), 1, 1, 0, 20));

        var page = service.listMilestones(null, 0, 0);

        assertEquals(5, page.content().get(0).requiredStampCount());
        assertEquals("M5", page.content().get(0).code());
    }
}
