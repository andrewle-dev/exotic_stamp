package metro.ExoticStamp.modules.auth.application.port;

import metro.ExoticStamp.modules.auth.domain.model.OtpType;

import java.util.Optional;

public interface OtpStorePort {

    void save(String email, OtpType type, String otp);

    Optional<String> find(String email, OtpType type);

    void delete(String email, OtpType type);

    boolean isOnCooldown(String email, OtpType type);

    void saveCooldown(String email, OtpType type);

    long getCooldownTtlSeconds(String email, OtpType type);

    boolean isMaxAttemptsExceeded(String email, OtpType type);

    void incrementAttempts(String email, OtpType type);

    int getAttemptsCount(String email, OtpType type);
}
