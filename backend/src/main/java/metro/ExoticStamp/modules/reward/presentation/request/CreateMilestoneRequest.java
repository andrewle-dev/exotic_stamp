package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import metro.ExoticStamp.modules.reward.presentation.dto.MilestoneStatusApi;
import metro.ExoticStamp.modules.reward.presentation.dto.RewardTypeApi;

import java.util.UUID;

@Data
public class CreateMilestoneRequest {

    @NotNull
    private UUID campaignId;

    @NotBlank
    @Size(max = 50)
    private String code;

    @NotNull
    @Min(1)
    private Integer requiredStampCount;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    private RewardTypeApi rewardType;

    @NotBlank
    @Size(max = 100)
    private String rewardTitle;

    @Size(max = 255)
    private String rewardDescription;

    @Size(max = 255)
    private String rewardImageUrl;

    private MilestoneStatusApi status;

    @Min(0)
    private Integer sortOrder;
}
