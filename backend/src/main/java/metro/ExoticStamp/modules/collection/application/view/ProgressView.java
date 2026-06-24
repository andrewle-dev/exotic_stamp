package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ProgressView(
        UUID lineId,
        long collected,
        long total,
        int percentage
) {
}
