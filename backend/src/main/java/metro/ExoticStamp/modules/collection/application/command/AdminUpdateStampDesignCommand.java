package metro.ExoticStamp.modules.collection.application.command;

import java.util.UUID;

public record AdminUpdateStampDesignCommand(
        UUID id,
        UUID stationId,
        UUID campaignId,
        String name,
        String artworkUrl,
        String animationUrl,
        String soundUrl,
        boolean limited,
        boolean active
) {
}
