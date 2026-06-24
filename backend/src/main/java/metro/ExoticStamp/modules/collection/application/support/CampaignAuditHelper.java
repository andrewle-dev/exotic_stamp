package metro.ExoticStamp.modules.collection.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.collection.domain.CollectionAuditConstants;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.rbac.application.support.RbacAuditIp;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CampaignAuditHelper {

    private final AuditLogService auditLogService;
    private final RbacSecurityContextHelper securityContextHelper;

    public void scheduleCampaignCreated(Campaign campaign) {
        schedule(CollectionAuditConstants.TABLE_CAMPAIGNS, CollectionAuditConstants.CAMPAIGN_CREATED,
                null, campaignSnapshot(campaign));
    }

    public void scheduleCampaignUpdated(Campaign campaign) {
        schedule(CollectionAuditConstants.TABLE_CAMPAIGNS, CollectionAuditConstants.CAMPAIGN_UPDATED,
                null, campaignSnapshot(campaign));
    }

    public void scheduleCampaignActivated(UUID campaignId) {
        schedule(CollectionAuditConstants.TABLE_CAMPAIGNS, CollectionAuditConstants.CAMPAIGN_ACTIVATED,
                null, Map.of("id", campaignId.toString()));
    }

    public void scheduleCampaignArchived(UUID campaignId) {
        schedule(CollectionAuditConstants.TABLE_CAMPAIGNS, CollectionAuditConstants.CAMPAIGN_ARCHIVED,
                null, Map.of("id", campaignId.toString()));
    }

    public void scheduleStationAssigned(UUID campaignId, UUID stationId) {
        schedule(CollectionAuditConstants.TABLE_CAMPAIGN_STATIONS, CollectionAuditConstants.CAMPAIGN_STATION_ASSIGNED,
                null, Map.of("campaignId", campaignId.toString(), "stationId", stationId.toString()));
    }

    public void scheduleStationRemoved(UUID campaignId, UUID stationId) {
        schedule(CollectionAuditConstants.TABLE_CAMPAIGN_STATIONS, CollectionAuditConstants.CAMPAIGN_STATION_REMOVED,
                null, Map.of("campaignId", campaignId.toString(), "stationId", stationId.toString()));
    }

    public void scheduleStampDesignCreated(StampDesign design) {
        schedule(CollectionAuditConstants.TABLE_STAMP_DESIGNS, CollectionAuditConstants.STAMP_DESIGN_CREATED,
                null, stampDesignSnapshot(design));
    }

    public void scheduleStampDesignUpdated(StampDesign design) {
        schedule(CollectionAuditConstants.TABLE_STAMP_DESIGNS, CollectionAuditConstants.STAMP_DESIGN_UPDATED,
                null, stampDesignSnapshot(design));
    }

    public void scheduleStampDesignDisabled(UUID designId) {
        schedule(CollectionAuditConstants.TABLE_STAMP_DESIGNS, CollectionAuditConstants.STAMP_DESIGN_DISABLED,
                null, Map.of("id", designId.toString()));
    }

    private void schedule(String table, String action, Object oldVal, Object newVal) {
        RbacTransactionCallbacks.afterCommit(() -> securityContextHelper.currentUserId().ifPresent(actorId ->
                auditLogService.log(actorId, table, action, oldVal, newVal, RbacAuditIp.UNKNOWN)));
    }

    private static Map<String, Object> campaignSnapshot(Campaign c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId() != null ? c.getId().toString() : null);
        m.put("code", c.getCode());
        m.put("name", c.getName());
        m.put("displayName", c.getDisplayName());
        m.put("status", c.getStatus() != null ? c.getStatus().name() : null);
        m.put("campaignType", c.getCampaignType() != null ? c.getCampaignType().name() : null);
        m.put("priority", c.getPriority());
        m.put("bannerImageUrl", imagePlaceholder(c.getBannerImageUrl()));
        m.put("thumbnailImageUrl", imagePlaceholder(c.getThumbnailImageUrl()));
        return m;
    }

    private static Map<String, Object> stampDesignSnapshot(StampDesign s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId() != null ? s.getId().toString() : null);
        m.put("campaignId", s.getCampaignId() != null ? s.getCampaignId().toString() : null);
        m.put("stationId", s.getStationId() != null ? s.getStationId().toString() : null);
        m.put("name", s.getName());
        m.put("status", s.getStatus() != null ? s.getStatus().name() : null);
        m.put("rarity", s.getRarity() != null ? s.getRarity().name() : null);
        m.put("sortOrder", s.getSortOrder());
        m.put("imageUrl", imagePlaceholder(s.getImageUrl()));
        m.put("previewImageUrl", imagePlaceholder(s.getPreviewImageUrl()));
        return m;
    }

    private static String imagePlaceholder(String url) {
        return url != null && !url.isBlank() ? "[image]" : null;
    }
}
