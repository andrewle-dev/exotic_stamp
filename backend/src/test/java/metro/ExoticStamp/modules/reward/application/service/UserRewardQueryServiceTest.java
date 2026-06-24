package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRewardQueryServiceTest {

    @Mock private UserRewardRepository userRewardRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private VoucherPoolRepository voucherPoolRepository;

    private UserRewardQueryService service;

    @BeforeEach
    void setUp() {
        RewardProperties props = new RewardProperties();
        props.setDefaultPageSize(20);
        props.setMaxPageSize(100);
        service = new UserRewardQueryService(
                userRewardRepository, milestoneRepository, voucherPoolRepository, new RewardAppMapper(), props);
    }

    @Test
    void pendingStock_detailOmitsVoucherCode() {
        UUID userId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UserReward ur = userReward(userId, userRewardId, milestoneId, RewardStatus.PENDING_STOCK, null);
        Milestone m = milestone(milestoneId);

        when(userRewardRepository.findByUserIdAndId(userId, userRewardId)).thenReturn(Optional.of(ur));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(m));

        UserRewardView view = service.getMyReward(userId, userRewardId);
        assertEquals(RewardStatus.PENDING_STOCK, view.status());
        assertNull(view.voucher());
    }

    @Test
    void issuedVoucher_detailIncludesCodeForOwner() {
        UUID userId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        UserReward ur = userReward(userId, userRewardId, milestoneId, RewardStatus.ISSUED, voucherId);
        Milestone m = milestone(milestoneId);
        VoucherPool vp = VoucherPool.builder()
                .id(voucherId)
                .milestoneId(milestoneId)
                .code("SECRET-CODE")
                .status(VoucherPoolStatus.CLAIMED)
                .assignedUserId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRewardRepository.findByUserIdAndId(userId, userRewardId)).thenReturn(Optional.of(ur));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(m));
        when(voucherPoolRepository.findById(voucherId)).thenReturn(Optional.of(vp));

        UserRewardView view = service.getMyReward(userId, userRewardId);
        assertEquals("SECRET-CODE", view.voucher().code());
    }

    @Test
    void otherUserCannotFetchReward() {
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        when(userRewardRepository.findByUserIdAndId(otherId, userRewardId)).thenReturn(Optional.empty());
        assertThrows(RewardNotFoundException.class, () -> service.getMyReward(otherId, userRewardId));
    }

    @Test
    void listNeverIncludesVoucherCode() {
        UUID userId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UserReward ur = userReward(userId, userRewardId, milestoneId, RewardStatus.ISSUED, UUID.randomUUID());
        when(userRewardRepository.findByUserIdOrderByIssuedAtDesc(userId, 0, 20))
                .thenReturn(new PagedSlice<>(List.of(ur), 1, 1, 0, 20));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone(milestoneId)));

        PageResponse<UserRewardView> page = service.listMyRewards(userId, null, 0, 20);
        assertNull(page.content().getFirst().voucher());
    }

    private static UserReward userReward(UUID userId, UUID id, UUID milestoneId, RewardStatus status, UUID voucherPoolId) {
        return UserReward.builder()
                .id(id)
                .userId(userId)
                .campaignId(UUID.randomUUID())
                .milestoneId(milestoneId)
                .voucherPoolId(voucherPoolId)
                .issuedAt(LocalDateTime.now())
                .status(status)
                .build();
    }

    private static Milestone milestone(UUID milestoneId) {
        return Milestone.builder()
                .id(milestoneId)
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(1)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .build();
    }
}
