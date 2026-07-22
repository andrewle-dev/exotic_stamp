package metro.ExoticStamp.modules.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.modules.auth.domain.model.AuthTransport;
import metro.ExoticStamp.modules.auth.domain.model.ClientPlatform;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {
    private String identifier;
    private String password;
    private String ipAddress;
    private String userAgent;
    /** App-generated device id (metadata). */
    private String deviceFingerprint;
    private AuthTransport transport;
    private ClientPlatform clientPlatform;
}
