package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.modules.auth.application.port.AccessTokenRevocationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenRevocationPortAdapter implements AccessTokenRevocationPort {

    private final AccessTokenRevocationRedisRepository repository;

    @Override
    public void addToDenylist(String jti, Duration accessTokenTtl) {
        repository.addToDenylist(jti, accessTokenTtl);
    }

    @Override
    public void setDeviceAccessJti(UUID userId, String deviceFingerprint, String jti) {
        repository.setDeviceAccessJti(userId, deviceFingerprint, jti);
    }

    @Override
    public Optional<String> getDeviceAccessJti(UUID userId, String deviceFingerprint) {
        return repository.getDeviceAccessJti(userId, deviceFingerprint);
    }

    @Override
    public void deleteDeviceAccessJti(UUID userId, String deviceFingerprint) {
        repository.deleteDeviceAccessJti(userId, deviceFingerprint);
    }

    @Override
    public void setCachedTokenVersion(UUID userId, long version) {
        repository.setCachedTokenVersion(userId, version);
    }
}
