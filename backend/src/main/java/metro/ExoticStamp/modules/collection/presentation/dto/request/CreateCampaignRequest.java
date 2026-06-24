package metro.ExoticStamp.modules.collection.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import metro.ExoticStamp.modules.collection.presentation.dto.CampaignTypeApi;

import java.time.LocalDateTime;

@Data
public class CreateCampaignRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String displayName;

    private String description;

    private CampaignTypeApi campaignType;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    private String bannerImageUrl;

    private String thumbnailImageUrl;

    private Integer priority;
}
