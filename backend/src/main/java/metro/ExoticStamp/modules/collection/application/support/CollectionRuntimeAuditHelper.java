package metro.ExoticStamp.modules.collection.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.collection.domain.CollectionAuditConstants;
import metro.ExoticStamp.modules.rbac.application.support.RbacAuditIp;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CollectionRuntimeAuditHelper {

    private final AuditLogService auditLogService;
    private final RbacSecurityContextHelper securityContextHelper;

    public void scheduleStampCollected(UUID userId, UUID stationId, UUID campaignId, UUID stampDesignId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId.toString());
        payload.put("stationId", stationId.toString());
        payload.put("campaignId", campaignId.toString());
        payload.put("stampDesignId", stampDesignId.toString());
        schedule(CollectionAuditConstants.TABLE_USER_STAMPS, CollectionAuditConstants.STAMP_COLLECTED, null, payload);
    }

    public void scheduleDuplicateAttempt(UUID userId, UUID stationId, UUID campaignId) {
        Map<String, Object> payload = Map.of(
                "userId", userId.toString(),
                "stationId", stationId.toString(),
                "campaignId", campaignId.toString()
        );
        schedule(CollectionAuditConstants.TABLE_USER_STAMPS, CollectionAuditConstants.STAMP_DUPLICATE_ATTEMPT, null, payload);
    }

    public void scheduleGpsValidationFailed(String errorCode, Double distanceMeters) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("errorCode", errorCode);
        if (distanceMeters != null) {
            payload.put("distanceMeters", Math.round(distanceMeters));
        }
        schedule(CollectionAuditConstants.TABLE_USER_STAMPS, CollectionAuditConstants.GPS_VALIDATION_FAILED, null, payload);
    }

    private void schedule(String table, String action, Object oldVal, Object newVal) {
        RbacTransactionCallbacks.afterCommit(() -> securityContextHelper.currentUserId().ifPresent(actorId ->
                auditLogService.log(actorId, table, action, oldVal, newVal, RbacAuditIp.UNKNOWN)));
    }
}
