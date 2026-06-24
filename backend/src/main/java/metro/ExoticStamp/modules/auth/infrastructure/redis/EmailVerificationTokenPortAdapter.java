package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.modules.auth.application.port.EmailVerificationTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenPortAdapter implements EmailVerificationTokenPort {

    private final VerifyTokenRepository repository;

    @Override
    public void saveToken(String token, UUID userId) {
        repository.saveToken(token, userId);
    }

    @Override
    public Optional<UUID> findUserIdByToken(String token) {
        return repository.findUserIdByToken(token);
    }

    @Override
    public void deleteToken(String token) {
        repository.deleteToken(token);
    }

    @Override
    public boolean isOnCooldown(String email) {
        return repository.isOnCooldown(email);
    }

    @Override
    public void saveCooldown(String email) {
        repository.saveCooldown(email);
    }

    @Override
    public long getCooldownTtlSeconds(String email) {
        return repository.getCooldownTtlSeconds(email);
    }
}
