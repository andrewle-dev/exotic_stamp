package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserRewardVoucherResponse(
        UUID id,
        String code
) {
}
