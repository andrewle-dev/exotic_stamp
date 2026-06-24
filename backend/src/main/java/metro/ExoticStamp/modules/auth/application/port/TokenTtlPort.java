package metro.ExoticStamp.modules.auth.application.port;

import java.time.Duration;

public interface TokenTtlPort {

    Duration getAccessTokenTtl();

    Duration getRefreshTokenTtl();
}
