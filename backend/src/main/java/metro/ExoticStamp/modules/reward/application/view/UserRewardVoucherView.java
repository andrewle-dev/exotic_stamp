package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserRewardVoucherView(
        UUID id,
        String code
) {
}
