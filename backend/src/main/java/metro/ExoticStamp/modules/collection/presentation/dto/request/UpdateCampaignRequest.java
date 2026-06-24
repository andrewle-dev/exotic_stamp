package metro.ExoticStamp.modules.collection.presentation.dto.request;

import lombok.Data;
import metro.ExoticStamp.modules.collection.presentation.dto.CampaignStatusApi;
import metro.ExoticStamp.modules.collection.presentation.dto.CampaignTypeApi;

import java.time.LocalDateTime;

@Data
public class UpdateCampaignRequest {

    private String code;
    private String name;
    private String displayName;
    private String description;
    private CampaignTypeApi campaignType;
    private CampaignStatusApi status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String bannerImageUrl;
    private String thumbnailImageUrl;
    private Integer priority;
}
