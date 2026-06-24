package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.modules.auth.application.port.RefreshTokenStorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

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
    public boolean isRevoked(String tokenHash) {
        return repository.isRevoked(tokenHash);
    }
}
