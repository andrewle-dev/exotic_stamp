package metro.ExoticStamp.modules.reward.application.service;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.port.UserStampCampaignCountPort;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import metro.ExoticStamp.modules.reward.domain.service.MilestoneDomainService;
import metro.ExoticStamp.modules.reward.application.service.VoucherAllocationService.VoucherAllocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardEvaluationServiceTest {

    @Mock private UserStampCampaignCountPort countPort;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private UserRewardRepository userRewardRepository;
    @Mock private RewardIssuancePolicyService issuancePolicyService;
    @Mock private VoucherAllocationService voucherAllocationService;
    @Mock private RewardAuditHelper rewardAuditHelper;
    @Mock private RewardCachePort rewardCachePort;
    @Mock private ApplicationEventPublisher eventPublisher;

    private RewardEvaluationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-04-12T10:00:00Z"), ZoneOffset.UTC);
        service = new RewardEvaluationService(
                countPort,
                milestoneRepository,
                userRewardRepository,
                issuancePolicyService,
                voucherAllocationService,
                rewardAuditHelper,
                new MilestoneDomainService(),
                rewardCachePort,
                eventPublisher,
                clock,
                new SimpleMeterRegistry()
        );
    }

    @Test
    void voucherAvailable_issuesWithVoucherId() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        Milestone milestone = milestone(userId, campaignId, milestoneId, 7, RewardType.VOUCHER, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(7L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());
        when(userRewardRepository.save(any(UserReward.class))).thenAnswer(inv -> {
            UserReward ur = inv.getArgument(0);
            ur.setId(UUID.randomUUID());
            return ur;
        });
        when(voucherAllocationService.allocate(eq(milestoneId), eq(userId), any(UUID.class)))
                .thenReturn(Optional.of(new VoucherAllocation(voucherId)));

        service.handleStampCollected(userId, campaignId);

        ArgumentCaptor<UserReward> cap = ArgumentCaptor.forClass(UserReward.class);
        verify(userRewardRepository, org.mockito.Mockito.times(2)).save(cap.capture());
        UserReward issued = cap.getAllValues().get(1);
        assertEquals(RewardStatus.ISSUED, issued.getStatus());
        assertEquals(voucherId, issued.getVoucherPoolId());
    }

    @Test
    void alreadyRewardedMilestone_skipped() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        Milestone milestone = milestone(userId, campaignId, milestoneId, 1, RewardType.DIGITAL_STICKER, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(5L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of(milestoneId));

        service.handleStampCollected(userId, campaignId);
        verify(userRewardRepository, never()).save(any());
    }

    @Test
    void issuesNonVoucherWhenThresholdMet() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        Milestone milestone = milestone(userId, campaignId, milestoneId, 3, RewardType.DIGITAL_STICKER, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(3L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());
        when(userRewardRepository.save(any(UserReward.class))).thenAnswer(inv -> {
            UserReward ur = inv.getArgument(0);
            ur.setId(UUID.randomUUID());
            return ur;
        });

        service.handleStampCollected(userId, campaignId);

        ArgumentCaptor<UserReward> cap = ArgumentCaptor.forClass(UserReward.class);
        verify(userRewardRepository).save(cap.capture());
        assertEquals(RewardStatus.ISSUED, cap.getValue().getStatus());
    }

    @Test
    void voucherEmptyPool_setsPendingStock() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        Milestone milestone = milestone(userId, campaignId, milestoneId, 1, RewardType.VOUCHER, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(1L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());
        when(userRewardRepository.save(any(UserReward.class))).thenAnswer(inv -> {
            UserReward ur = inv.getArgument(0);
            ur.setId(UUID.randomUUID());
            return ur;
        });
        when(voucherAllocationService.allocate(eq(milestoneId), eq(userId), any(UUID.class)))
                .thenReturn(Optional.empty());

        service.handleStampCollected(userId, campaignId);

        ArgumentCaptor<UserReward> cap = ArgumentCaptor.forClass(UserReward.class);
        verify(userRewardRepository).save(cap.capture());
        assertEquals(RewardStatus.PENDING_STOCK, cap.getValue().getStatus());
    }

    @Test
    void belowThreshold_issuesNothing() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        Milestone milestone = milestone(userId, campaignId, UUID.randomUUID(), 5, RewardType.DIGITAL_STICKER, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(2L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());

        service.handleStampCollected(userId, campaignId);
        verify(userRewardRepository, never()).save(any());
    }

    @Test
    void duplicateUniqueConstraint_skips() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        Milestone milestone = milestone(userId, campaignId, milestoneId, 1, RewardType.DIGITAL_STICKER, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(1L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());
        when(userRewardRepository.save(any(UserReward.class)))
                .thenThrow(new DataIntegrityViolationException("uq_user_rewards_once"));

        service.handleStampCollected(userId, campaignId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void oneCollectCrossingMultipleMilestones_issuesMultipleRewards() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();
        Milestone milestone1 = milestone(userId, campaignId, m1, 1, RewardType.DIGITAL_STICKER, MilestoneStatus.ACTIVE);
        Milestone milestone2 = milestone(userId, campaignId, m2, 2, RewardType.DIGITAL_BADGE, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(2L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone1, milestone2));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());
        when(userRewardRepository.save(any(UserReward.class))).thenAnswer(inv -> {
            UserReward ur = inv.getArgument(0);
            ur.setId(UUID.randomUUID());
            return ur;
        });

        service.handleStampCollected(userId, campaignId);

        verify(userRewardRepository, org.mockito.Mockito.times(2)).save(any(UserReward.class));
    }

    @Test
    void inactiveMilestone_skipped() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        Milestone inactive = milestone(userId, campaignId, UUID.randomUUID(), 1,
                RewardType.DIGITAL_STICKER, MilestoneStatus.INACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(5L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of());
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());

        service.handleStampCollected(userId, campaignId);
        verify(userRewardRepository, never()).save(any());
    }

    @Test
    void voucherPoolLinkUniqueRace_releasesAndLeavesPendingStock() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        Milestone milestone = milestone(userId, campaignId, milestoneId, 1, RewardType.VOUCHER, MilestoneStatus.ACTIVE);

        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(1L);
        when(milestoneRepository.findActiveByCampaignId(campaignId)).thenReturn(List.of(milestone));
        when(userRewardRepository.findMilestoneIdsRewardedForUser(userId)).thenReturn(Set.of());
        when(userRewardRepository.save(any(UserReward.class))).thenAnswer(inv -> {
            UserReward ur = inv.getArgument(0);
            if (ur.getId() == null) {
                ur.setId(UUID.randomUUID());
            }
            if (ur.getVoucherPoolId() != null) {
                throw new DataIntegrityViolationException(
                        "duplicate", new RuntimeException("uq_user_rewards_voucher_pool_id"));
            }
            return ur;
        });
        when(voucherAllocationService.allocate(eq(milestoneId), eq(userId), any(UUID.class)))
                .thenReturn(Optional.of(new VoucherAllocation(voucherId)));

        service.handleStampCollected(userId, campaignId);

        verify(voucherAllocationService).release(voucherId);
        ArgumentCaptor<UserReward> cap = ArgumentCaptor.forClass(UserReward.class);
        verify(userRewardRepository, org.mockito.Mockito.atLeast(2)).save(cap.capture());
        UserReward last = cap.getAllValues().get(cap.getAllValues().size() - 1);
        assertEquals(RewardStatus.PENDING_STOCK, last.getStatus());
        assertEquals(null, last.getVoucherPoolId());
        verify(rewardAuditHelper).scheduleRewardPendingStock(any(UUID.class));
    }

    @Test
    void fulfillPendingStock_fulfilledWhenVoucherAvailable() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        UserReward pending = pendingReward(userId, campaignId, milestoneId, userRewardId);
        Milestone milestone = milestone(userId, campaignId, milestoneId, 2, RewardType.VOUCHER, MilestoneStatus.ACTIVE);

        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.of(pending));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));
        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(2L);
        when(voucherAllocationService.allocate(milestoneId, userId, userRewardId))
                .thenReturn(Optional.of(new VoucherAllocation(voucherId)));
        when(userRewardRepository.save(any(UserReward.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.fulfillPendingStock(userRewardId);

        assertEquals(RewardEvaluationService.PendingStockResult.FULFILLED, result);
        assertEquals(RewardStatus.ISSUED, pending.getStatus());
        assertEquals(voucherId, pending.getVoucherPoolId());
        verify(rewardCachePort).evictUserRewardListAll(userId);
    }

    @Test
    void fulfillPendingStock_stillNoStockWhenPoolEmpty() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UserReward pending = pendingReward(userId, campaignId, milestoneId, userRewardId);
        Milestone milestone = milestone(userId, campaignId, milestoneId, 1, RewardType.VOUCHER, MilestoneStatus.ACTIVE);

        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.of(pending));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));
        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(1L);
        when(voucherAllocationService.allocate(milestoneId, userId, userRewardId)).thenReturn(Optional.empty());

        var result = service.fulfillPendingStock(userRewardId);

        assertEquals(RewardEvaluationService.PendingStockResult.STILL_NO_STOCK, result);
        assertEquals(RewardStatus.PENDING_STOCK, pending.getStatus());
    }

    @Test
    void fulfillPendingStock_skippedWhenAlreadyIssued() {
        UUID userRewardId = UUID.randomUUID();
        UserReward issued = UserReward.builder()
                .id(userRewardId)
                .userId(UUID.randomUUID())
                .campaignId(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .issuedAt(LocalDateTime.now())
                .status(RewardStatus.ISSUED)
                .build();
        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.of(issued));

        var result = service.fulfillPendingStock(userRewardId);

        assertEquals(RewardEvaluationService.PendingStockResult.SKIPPED, result);
        verify(voucherAllocationService, never()).allocate(any(), any(), any());
    }

    @Test
    void fulfillPendingStock_skippedWhenRewardMissing() {
        UUID userRewardId = UUID.randomUUID();
        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.empty());

        assertEquals(RewardEvaluationService.PendingStockResult.SKIPPED, service.fulfillPendingStock(userRewardId));
        assertEquals(RewardEvaluationService.PendingStockResult.SKIPPED, service.fulfillPendingStock(null));
    }

    @Test
    void fulfillPendingStock_skippedWhenVoucherAlreadyLinked() {
        UUID userRewardId = UUID.randomUUID();
        UserReward pending = UserReward.builder()
                .id(userRewardId)
                .userId(UUID.randomUUID())
                .campaignId(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .voucherPoolId(UUID.randomUUID())
                .issuedAt(LocalDateTime.now())
                .status(RewardStatus.PENDING_STOCK)
                .build();
        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.of(pending));

        assertEquals(RewardEvaluationService.PendingStockResult.SKIPPED, service.fulfillPendingStock(userRewardId));
    }

    @Test
    void fulfillPendingStock_skippedWhenMilestoneNotVoucher() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UserReward pending = pendingReward(userId, campaignId, milestoneId, userRewardId);
        Milestone milestone = milestone(userId, campaignId, milestoneId, 1, RewardType.DIGITAL_STICKER, MilestoneStatus.ACTIVE);

        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.of(pending));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));

        assertEquals(RewardEvaluationService.PendingStockResult.SKIPPED, service.fulfillPendingStock(userRewardId));
    }

    @Test
    void fulfillPendingStock_skippedWhenStampThresholdNotMet() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UserReward pending = pendingReward(userId, campaignId, milestoneId, userRewardId);
        Milestone milestone = milestone(userId, campaignId, milestoneId, 5, RewardType.VOUCHER, MilestoneStatus.ACTIVE);

        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.of(pending));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));
        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(2L);

        assertEquals(RewardEvaluationService.PendingStockResult.SKIPPED, service.fulfillPendingStock(userRewardId));
    }

    @Test
    void fulfillPendingStock_stillNoStockOnVoucherLinkRace() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        UserReward pending = pendingReward(userId, campaignId, milestoneId, userRewardId);
        Milestone milestone = milestone(userId, campaignId, milestoneId, 1, RewardType.VOUCHER, MilestoneStatus.ACTIVE);

        when(userRewardRepository.findById(userRewardId)).thenReturn(Optional.of(pending));
        when(milestoneRepository.findById(milestoneId)).thenReturn(Optional.of(milestone));
        when(countPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId)).thenReturn(1L);
        when(voucherAllocationService.allocate(milestoneId, userId, userRewardId))
                .thenReturn(Optional.of(new VoucherAllocation(voucherId)));
        when(userRewardRepository.save(any(UserReward.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "duplicate", new RuntimeException("uq_user_rewards_voucher_pool_id")))
                .thenAnswer(inv -> inv.getArgument(0));

        var result = service.fulfillPendingStock(userRewardId);

        assertEquals(RewardEvaluationService.PendingStockResult.STILL_NO_STOCK, result);
        verify(voucherAllocationService).release(voucherId);
        assertEquals(RewardStatus.PENDING_STOCK, pending.getStatus());
        assertEquals(null, pending.getVoucherPoolId());
    }

    private static UserReward pendingReward(UUID userId, UUID campaignId, UUID milestoneId, UUID userRewardId) {
        return UserReward.builder()
                .id(userRewardId)
                .userId(userId)
                .campaignId(campaignId)
                .milestoneId(milestoneId)
                .issuedAt(LocalDateTime.now())
                .status(RewardStatus.PENDING_STOCK)
                .build();
    }

    private static Milestone milestone(UUID userId, UUID campaignId, UUID id, int stamps, RewardType type, MilestoneStatus status) {
        return Milestone.builder()
                .id(id)
                .campaignId(campaignId)
                .code("M" + id.toString().substring(0, 4))
                .stampsRequired(stamps)
                .name("Milestone")
                .rewardType(type)
                .rewardTitle("Reward")
                .status(status)
                .sortOrder(0)
                .build();
    }
}
