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
                saved = userRewardRepository.save(saved);
                rewardAuditHelper.scheduleVoucherAllocated(allocation.get().voucherPoolId(), saved.getId());
                rewardAuditHelper.scheduleRewardIssued(saved.getId());
            } else {
                rewardAuditHelper.scheduleVoucherStockEmpty(milestoneId);
                rewardAuditHelper.scheduleRewardPendingStock(saved.getId());
            }
        } else {
            rewardAuditHelper.scheduleRewardIssued(saved.getId());
        }

        meterRegistry.counter("reward.issued", "rewardType", rewardType.name()).increment();
        rewardCachePort.evictUserRewardListAll(userId);
        rewardCachePort.evictUserRewardDetail(userId, saved.getId());

        UserReward finalSaved = saved;
        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(RewardIssuedEvent.milestoneIssued(
                        userId, finalSaved.getId(), milestoneId, rewardType));
            } catch (Exception e) {
                log.error("[Reward] RewardIssuedEvent publish failed userId={} milestoneId={}: {}",
                        userId, milestoneId, e.getMessage(), e);
            }
        });
    }

    private static boolean isUserRewardUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_user_rewards_once");
    }
}
