package metro.ExoticStamp.modules.auth.application.port;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface AccessTokenRevocationPort {

    void addToDenylist(String jti, Duration accessTokenTtl);

    void setDeviceAccessJti(UUID userId, String deviceFingerprint, String jti);

    Optional<String> getDeviceAccessJti(UUID userId, String deviceFingerprint);

    void deleteDeviceAccessJti(UUID userId, String deviceFingerprint);

    void setCachedTokenVersion(UUID userId, long version);
}
