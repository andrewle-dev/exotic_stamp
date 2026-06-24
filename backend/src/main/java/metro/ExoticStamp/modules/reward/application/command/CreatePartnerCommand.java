package metro.ExoticStamp.modules.reward.application.command;

import java.time.LocalDate;

public record CreatePartnerCommand(
        String name,
        String logoUrl,
        String contactEmail,
        LocalDate contractStartDate,
        LocalDate contractEndDate
) {
}
