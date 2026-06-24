package metro.ExoticStamp.modules.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginCommand {
    private String identifier;           // email or username
    private String password;
    private String ipAddress;           // from HttpServletRequest.getRemoteAddr()
    private String userAgent;           // from request header "User-Agent"
    private String deviceFingerprint; // optional, for multi-device tracking
}

