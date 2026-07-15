package metro.ExoticStamp.modules.metro.application.command;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class RevokeStationScanKeyCommand {
    UUID id;
    String reason;
}
