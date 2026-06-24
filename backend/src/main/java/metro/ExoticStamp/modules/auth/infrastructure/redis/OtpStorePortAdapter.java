package metro.ExoticStamp.modules.auth.infrastructure.redis;

import metro.ExoticStamp.modules.auth.application.port.OtpStorePort;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OtpStorePortAdapter implements OtpStorePort {

    private final OtpRepository repository;

    @Override
    public void save(String email, OtpType type, String otp) {
        repository.save(email, type, otp);
    }

    @Override
    public Optional<String> find(String email, OtpType type) {
        return repository.find(email, type);
    }

    @Override
    public void delete(String email, OtpType type) {
        repository.delete(email, type);
    }

    @Override
    public boolean isOnCooldown(String email, OtpType type) {
        return repository.isOnCooldown(email, type);
    }

    @Override
    public void saveCooldown(String email, OtpType type) {
        repository.saveCooldown(email, type);
    }

    @Override
    public long getCooldownTtlSeconds(String email, OtpType type) {
        return repository.getCooldownTtlSeconds(email, type);
    }

    @Override
    public boolean isMaxAttemptsExceeded(String email, OtpType type) {
        return repository.isMaxAttemptsExceeded(email, type);
    }

    @Override
    public void incrementAttempts(String email, OtpType type) {
        repository.incrementAttempts(email, type);
    }

    @Override
    public int getAttemptsCount(String email, OtpType type) {
        return repository.getAttemptsCount(email, type);
    }
}
