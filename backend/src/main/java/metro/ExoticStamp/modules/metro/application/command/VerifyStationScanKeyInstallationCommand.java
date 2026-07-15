package metro.ExoticStamp.modules.metro.application.command;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class VerifyStationScanKeyInstallationCommand {
    UUID id;
    String payloadReadBack;
    Double latitude;
    Double longitude;
    Double accuracyMeters;
    String devicePlatform;
    String appVersion;
}
