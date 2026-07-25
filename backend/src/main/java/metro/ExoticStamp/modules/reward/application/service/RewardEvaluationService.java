package metro.ExoticStamp.modules.reward.application.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.port.UserStampCampaignCountPort;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.domain.event.RewardIssuedEvent;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyIssuedException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import metro.ExoticStamp.modules.reward.domain.service.MilestoneDomainService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardEvaluationService {

    private final UserStampCampaignCountPort userStampCampaignCountPort;
    private final MilestoneRepository milestoneRepository;
    private final UserRewardRepository userRewardRepository;
    private final RewardIssuancePolicyService issuancePolicyService;
    private final VoucherAllocationService voucherAllocationService;
    private final RewardAuditHelper rewardAuditHelper;
    private final MilestoneDomainService milestoneDomainService;
    private final RewardCachePort rewardCachePort;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final MeterRegistry meterRegistry;

    @Transactional
    public void handleStampCollected(UUID userId, UUID campaignId) {
        if (userId == null || campaignId == null) {
            log.warn("[Reward] handleStampCollected skipped: missing userId or campaignId");
            return;
        }
        long stampCount = userStampCampaignCountPort.countDistinctStationsByUserIdAndCampaignId(userId, campaignId);
        List<Milestone> applicable = milestoneRepository.findActiveByCampaignId(campaignId);
        Set<UUID> rewardedMilestoneIds = userRewardRepository.findMilestoneIdsRewardedForUser(userId);
        List<Milestone> targets = milestoneDomainService.findNewlyCompletedMilestones(
                stampCount, applicable, rewardedMilestoneIds);
        for (Milestone milestone : targets) {
            try {
                issueForMilestone(userId, milestone);
            } catch (RewardAlreadyIssuedException ex) {
                log.debug("[Reward] skip already issued userId={} milestoneId={}", userId, milestone.getId());
            } catch (Exception ex) {
                log.error("[Reward] issuance failed userId={} milestoneId={}: {}",
                        userId, milestone.getId(), ex.getMessage(), ex);
            }
        }
    }

    private void issueForMilestone(UUID userId, Milestone milestone) {
        UUID milestoneId = milestone.getId();
        if (!milestone.isEvaluable()) {
            return;
        }
        issuancePolicyService.assertNotAlreadyIssued(userId, milestoneId);

        LocalDateTime issuedAt = LocalDateTime.now(clock);
        RewardType rewardType = milestone.getRewardType();
        RewardStatus initialStatus = rewardType == RewardType.VOUCHER
                ? RewardStatus.PENDING_STOCK
                : RewardStatus.ISSUED;

        UserReward toSave = UserReward.builder()
                .userId(userId)
                .campaignId(milestone.getCampaignId())
                .milestoneId(milestoneId)
                .issuedAt(issuedAt)
                .status(initialStatus)
                .build();

        UserReward saved;
        try {
            saved = userRewardRepository.save(toSave);
        } catch (DataIntegrityViolationException ex) {
            if (isUserRewardUniqueViolation(ex)) {
                log.debug("[Reward] duplicate user_reward skipped userId={} milestoneId={}", userId, milestoneId);
                return;
            }
            throw ex;
        }

        if (rewardType == RewardType.VOUCHER) {
            var allocation = voucherAllocationService.allocate(milestoneId, userId, saved.getId());
            if (allocation.isPresent()) {
                saved.setStatus(RewardStatus.ISSUED);
                saved.setVoucherPoolId(allocation.get().voucherPoolId());
                try {
                    saved = userRewardRepository.save(saved);
                    rewardAuditHelper.scheduleVoucherAllocated(allocation.get().voucherPoolId(), saved.getId());
                    rewardAuditHelper.scheduleRewardIssued(saved.getId());
                } catch (DataIntegrityViolationException ex) {
                    if (!isVoucherPoolLinkUniqueViolation(ex)) {
                        throw ex;
                    }
                    log.warn("[Reward] voucher_pool_id unique race userId={} milestoneId={} voucherPoolId={}",
                            userId, milestoneId, allocation.get().voucherPoolId());
                    voucherAllocationService.release(allocation.get().voucherPoolId());
                    saved.setVoucherPoolId(null);
                    saved.setStatus(RewardStatus.PENDING_STOCK);
                    saved = userRewardRepository.save(saved);
                    rewardAuditHelper.scheduleVoucherStockEmpty(milestoneId);
                    rewardAuditHelper.scheduleRewardPendingStock(saved.getId());
                }
            } else {
                rewardAuditHelper.scheduleVoucherStockEmpty(milestoneId);
                rewardAuditHelper.scheduleRewardPendingStock(saved.getId());
            }
        } else {
            rewardAuditHelper.scheduleRewardIssued(saved.getId());
        }

        if (saved.getStatus() == RewardStatus.ISSUED) {
            meterRegistry.counter("reward.issued", "rewardType", rewardType.name()).increment();
            publishIssuedEventAfterCommit(userId, saved.getId(), milestoneId, rewardType);
        }
        rewardCachePort.evictUserRewardListAll(userId);
        rewardCachePort.evictUserRewardDetail(userId, saved.getId());
    }

    /**
     * Fulfill an existing PENDING_STOCK reward when voucher stock becomes available.
     * Does not create a new user_rewards row.
     */
    @Transactional
    public PendingStockResult fulfillPendingStock(UUID userRewardId) {
        if (userRewardId == null) {
            return PendingStockResult.SKIPPED;
        }
        UserReward reward = userRewardRepository.findById(userRewardId).orElse(null);
        if (reward == null) {
            return PendingStockResult.SKIPPED;
        }
        if (reward.getStatus() != RewardStatus.PENDING_STOCK || reward.getVoucherPoolId() != null) {
            return PendingStockResult.SKIPPED;
        }
        Milestone milestone = milestoneRepository.findById(reward.getMilestoneId()).orElse(null);
        if (milestone == null || !milestone.isEvaluable() || milestone.getRewardType() != RewardType.VOUCHER) {
            return PendingStockResult.SKIPPED;
        }
        long stampCount = userStampCampaignCountPort.countDistinctStationsByUserIdAndCampaignId(
                reward.getUserId(), reward.getCampaignId());
        if (stampCount < milestone.getStampsRequired()) {
            return PendingStockResult.SKIPPED;
        }

        var allocation = voucherAllocationService.allocate(
                milestone.getId(), reward.getUserId(), reward.getId());
        if (allocation.isEmpty()) {
            return PendingStockResult.STILL_NO_STOCK;
        }
        reward.setStatus(RewardStatus.ISSUED);
        reward.setVoucherPoolId(allocation.get().voucherPoolId());
        try {
            userRewardRepository.save(reward);
        } catch (DataIntegrityViolationException ex) {
            if (!isVoucherPoolLinkUniqueViolation(ex)) {
                throw ex;
            }
            voucherAllocationService.release(allocation.get().voucherPoolId());
            reward.setVoucherPoolId(null);
            reward.setStatus(RewardStatus.PENDING_STOCK);
            userRewardRepository.save(reward);
            return PendingStockResult.STILL_NO_STOCK;
        }
        rewardAuditHelper.scheduleVoucherAllocated(allocation.get().voucherPoolId(), reward.getId());
        rewardAuditHelper.scheduleRewardIssued(reward.getId());
        meterRegistry.counter("reward.issued", "rewardType", RewardType.VOUCHER.name()).increment();
        rewardCachePort.evictUserRewardListAll(reward.getUserId());
        rewardCachePort.evictUserRewardDetail(reward.getUserId(), reward.getId());
        publishIssuedEventAfterCommit(
                reward.getUserId(), reward.getId(), milestone.getId(), RewardType.VOUCHER);
        return PendingStockResult.FULFILLED;
    }

    private void publishIssuedEventAfterCommit(
            UUID userId, UUID userRewardId, UUID milestoneId, RewardType rewardType) {
        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(RewardIssuedEvent.milestoneIssued(
                        userId, userRewardId, milestoneId, rewardType));
            } catch (Exception e) {
                log.error("[Reward] RewardIssuedEvent publish failed userId={} milestoneId={}: {}",
                        userId, milestoneId, e.getClass().getSimpleName());
            }
        });
    }

    public enum PendingStockResult {
        FULFILLED,
        STILL_NO_STOCK,
        SKIPPED
    }

    private static boolean isUserRewardUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_user_rewards_once");
    }

    private static boolean isVoucherPoolLinkUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_user_rewards_voucher_pool_id");
    }
}
