package metro.ExoticStamp.modules.reward.application.view;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record PartnerView(
        UUID id,
        String name,
        String logoUrl,
        String contactEmail,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        boolean active
) {
}
