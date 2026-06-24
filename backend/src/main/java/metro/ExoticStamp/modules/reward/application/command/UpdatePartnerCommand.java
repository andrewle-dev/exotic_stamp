package metro.ExoticStamp.modules.reward.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record UpdatePartnerCommand(
        UUID id,
        String name,
        String logoUrl,
        String contactEmail,
        LocalDate contractStartDate,
        LocalDate contractEndDate
) {
}
