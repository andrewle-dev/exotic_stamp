package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import metro.ExoticStamp.modules.reward.presentation.dto.RewardTypeApi;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateRewardRequest {

    @NotNull
    private UUID milestoneId;

    private UUID partnerId;

    @NotNull
    private RewardTypeApi rewardType;

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

    private BigDecimal valueAmount;

    private Integer expiryDays;

    private Integer totalStock;
}
