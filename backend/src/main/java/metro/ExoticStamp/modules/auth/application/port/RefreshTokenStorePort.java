package metro.ExoticStamp.modules.auth.application.port;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStorePort {

    void save(UUID userId, String deviceFingerprint, String tokenHash);

    void revoke(UUID userId, String deviceFingerprint, String tokenHash);

    void revokeAllForUser(UUID userId);

    /**
     * Returns true only when Redis explicitly has a revoked key.
     * On Redis errors returns false — callers must rely on DB session state
     * and must not escalate to reuse-attack.
     */
    boolean isKnownRevoked(String tokenHash);

    /** True when Redis operations succeeded for a recent probe. */
    boolean isHealthy();

    void putGraceCredentials(String oldTokenHash, String accessToken, String refreshToken, Duration ttl);

    Optional<GraceCredentials> findGraceCredentials(String oldTokenHash);

    record GraceCredentials(String accessToken, String refreshToken) {}
}
