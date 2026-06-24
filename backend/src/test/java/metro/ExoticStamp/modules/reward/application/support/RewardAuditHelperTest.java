package metro.ExoticStamp.modules.reward.application.support;

import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.reward.domain.RewardAuditConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardAuditHelperTest {

    @Mock private AuditLogService auditLogService;
    @Mock private RbacSecurityContextHelper securityContextHelper;
    @InjectMocks private RewardAuditHelper rewardAuditHelper;

    @Test
    void voucherImported_auditPayloadNeverContainsVoucherCode() {
        UUID actorId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        when(securityContextHelper.currentUserId()).thenReturn(Optional.of(actorId));

        TransactionSynchronizationManager.initSynchronization();
        try {
            rewardAuditHelper.scheduleVoucherImported(milestoneId, 3);
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCommit());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        ArgumentCaptor<String> newValue = ArgumentCaptor.forClass(String.class);
        verify(auditLogService).log(eq(actorId), eq(RewardAuditConstants.TABLE_VOUCHER_POOL),
                eq(RewardAuditConstants.VOUCHER_IMPORTED), eq(null), newValue.capture(), any());
        assertFalse(newValue.getValue().contains("SECRET"));
        assertFalse(newValue.getValue().contains("CODE"));
    }
}
