package metro.ExoticStamp.modules.auth.application.port;

import java.util.UUID;

public interface RefreshTokenStorePort {

    void save(UUID userId, String deviceFingerprint, String tokenHash);

    void revoke(UUID userId, String deviceFingerprint, String tokenHash);

    void revokeAllForUser(UUID userId);

    boolean isRevoked(String tokenHash);
}
