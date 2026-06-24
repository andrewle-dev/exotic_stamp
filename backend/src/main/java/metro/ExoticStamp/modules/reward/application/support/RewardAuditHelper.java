package metro.ExoticStamp.modules.reward.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.rbac.application.support.RbacAuditIp;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.reward.domain.RewardAuditConstants;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RewardAuditHelper {

    private final AuditLogService auditLogService;
    private final RbacSecurityContextHelper securityContextHelper;

    public void schedule(String table, String action, String oldValue, String newValue) {
        RbacTransactionCallbacks.afterCommit(() -> securityContextHelper.currentUserId().ifPresent(actorId ->
                auditLogService.log(actorId, table, action, oldValue, newValue, RbacAuditIp.UNKNOWN)));
    }

    public void scheduleRewardIssued(UUID userRewardId) {
        schedule(RewardAuditConstants.TABLE_USER_REWARDS, RewardAuditConstants.REWARD_ISSUED, null, userRewardId.toString());
    }

    public void scheduleRewardPendingStock(UUID userRewardId) {
        schedule(RewardAuditConstants.TABLE_USER_REWARDS, RewardAuditConstants.REWARD_PENDING_STOCK, null, userRewardId.toString());
    }

    public void scheduleVoucherAllocated(UUID voucherPoolId, UUID userRewardId) {
        schedule(RewardAuditConstants.TABLE_VOUCHER_POOL, RewardAuditConstants.VOUCHER_ALLOCATED,
                voucherPoolId.toString(), userRewardId.toString());
    }

    public void scheduleVoucherStockEmpty(UUID milestoneId) {
        schedule(RewardAuditConstants.TABLE_VOUCHER_POOL, RewardAuditConstants.VOUCHER_STOCK_EMPTY, null, milestoneId.toString());
    }

    public void scheduleMilestoneCreated(UUID milestoneId) {
        schedule(RewardAuditConstants.TABLE_MILESTONES, RewardAuditConstants.MILESTONE_CREATED, null, milestoneId.toString());
    }

    public void scheduleMilestoneUpdated(UUID milestoneId) {
        schedule(RewardAuditConstants.TABLE_MILESTONES, RewardAuditConstants.MILESTONE_UPDATED, null, milestoneId.toString());
    }

    public void scheduleMilestoneDisabled(UUID milestoneId) {
        schedule(RewardAuditConstants.TABLE_MILESTONES, RewardAuditConstants.MILESTONE_DISABLED, milestoneId.toString(), null);
    }

    public void scheduleVoucherImported(UUID milestoneId, int count) {
        schedule(RewardAuditConstants.TABLE_VOUCHER_POOL, RewardAuditConstants.VOUCHER_IMPORTED,
                null, milestoneId + ":count=" + count);
    }

    public void scheduleVoucherDisabled(UUID voucherPoolId) {
        schedule(RewardAuditConstants.TABLE_VOUCHER_POOL, RewardAuditConstants.VOUCHER_DISABLED, voucherPoolId.toString(), null);
    }
}
