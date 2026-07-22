package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.modules.auth.application.port.RefreshTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenStorePortAdapter implements RefreshTokenStorePort {

    private final RefreshTokenRedisRepository repository;

    @Override
    public void save(UUID userId, String deviceFingerprint, String tokenHash) {
        repository.save(userId, deviceFingerprint, tokenHash);
    }

    @Override
    public void revoke(UUID userId, String deviceFingerprint, String tokenHash) {
        repository.revoke(userId, deviceFingerprint, tokenHash);
    }

    @Override
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId);
    }

    @Override
    public boolean isKnownRevoked(String tokenHash) {
        return repository.isKnownRevoked(tokenHash);
    }

    @Override
    public boolean isHealthy() {
        return repository.isHealthy();
    }

    @Override
    public void putGraceCredentials(String oldTokenHash, String accessToken, String refreshToken, Duration ttl) {
        repository.putGraceCredentials(oldTokenHash, accessToken, refreshToken, ttl);
    }

    @Override
    public Optional<GraceCredentials> findGraceCredentials(String oldTokenHash) {
        return repository.findGraceCredentials(oldTokenHash)
                .map(p -> new GraceCredentials(p.accessToken(), p.refreshToken()));
    }
}
