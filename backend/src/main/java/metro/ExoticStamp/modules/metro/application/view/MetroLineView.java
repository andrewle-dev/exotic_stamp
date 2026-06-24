package metro.ExoticStamp.modules.metro.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record MetroLineView(
        UUID id,
        String code,
        String name,
        boolean active
) {
}
