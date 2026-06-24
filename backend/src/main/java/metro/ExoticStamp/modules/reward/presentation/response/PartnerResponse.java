package metro.ExoticStamp.modules.reward.presentation.response;

import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record PartnerResponse(
        UUID id,
        String name,
        String logoUrl,
        String contactEmail,
        LocalDate contractStartDate,
        LocalDate contractEndDate,
        boolean active
) {
}
