package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record CollectionAdminStatsView(
        long totalStampsCollected,
        List<CampaignStampCountView> stampsPerCampaign
) {
    public record CampaignStampCountView(UUID campaignId, long stampCount) {
    }
}
