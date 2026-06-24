package metro.ExoticStamp.modules.collection.presentation.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ActiveCampaignListResponse(
        List<ActiveCampaignResponse> campaigns
) {
}
