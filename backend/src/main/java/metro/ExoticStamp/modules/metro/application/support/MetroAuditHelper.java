package metro.ExoticStamp.modules.metro.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.metro.domain.MetroAuditConstants;
import metro.ExoticStamp.modules.rbac.application.support.RbacAuditIp;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetroAuditHelper {

    private final AuditLogService auditLogService;
    private final RbacSecurityContextHelper securityContextHelper;

    public void schedule(String table, String action, String oldValue, String newValue) {
        RbacTransactionCallbacks.afterCommit(() -> securityContextHelper.currentUserId().ifPresent(actorId ->
                auditLogService.log(actorId, table, action, oldValue, newValue, RbacAuditIp.UNKNOWN)));
    }

    public void scheduleLineCreated(String lineId) {
        schedule(MetroAuditConstants.TABLE_LINES, MetroAuditConstants.LINE_CREATED, null, lineId);
    }

    public void scheduleLineUpdated(String lineId) {
        schedule(MetroAuditConstants.TABLE_LINES, MetroAuditConstants.LINE_UPDATED, null, lineId);
    }

    public void scheduleLineDisabled(String lineId) {
        schedule(MetroAuditConstants.TABLE_LINES, MetroAuditConstants.LINE_DISABLED, lineId, null);
    }

    public void scheduleStationCreated(String stationId) {
        schedule(MetroAuditConstants.TABLE_STATIONS, MetroAuditConstants.STATION_CREATED, null, stationId);
    }

    public void scheduleStationUpdated(String stationId) {
        schedule(MetroAuditConstants.TABLE_STATIONS, MetroAuditConstants.STATION_UPDATED, null, stationId);
    }

    public void scheduleStationDisabled(String stationId) {
        schedule(MetroAuditConstants.TABLE_STATIONS, MetroAuditConstants.STATION_DISABLED, stationId, null);
    }

    public void scheduleScanKeyUpdated(String stationId, String redactedSummary) {
        schedule(MetroAuditConstants.TABLE_STATIONS, MetroAuditConstants.SCAN_KEY_UPDATED, null,
                stationId + ":" + redactedSummary);
    }

    public void scheduleQrRotated(String stationId) {
        schedule(MetroAuditConstants.TABLE_STATIONS, MetroAuditConstants.QR_ROTATED, null, stationId);
    }

    public void schedulePublicAssetUploaded(String url) {
        schedule(MetroAuditConstants.TABLE_UPLOADS, MetroAuditConstants.PUBLIC_ASSET_UPLOADED, null, url);
    }
}
