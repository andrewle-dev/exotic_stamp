package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import metro.ExoticStamp.modules.reward.presentation.dto.MilestoneStatusApi;
import metro.ExoticStamp.modules.reward.presentation.dto.RewardTypeApi;

@Data
public class UpdateMilestoneRequest {

    @Size(max = 50)
    private String code;

    @Min(1)
    private Integer requiredStampCount;

    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    private RewardTypeApi rewardType;

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
