package metro.ExoticStamp.modules.auth.application.port;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenPort {

    void saveToken(String token, UUID userId);

    Optional<UUID> findUserIdByToken(String token);

    void deleteToken(String token);

    boolean isOnCooldown(String email);

    void saveCooldown(String email);

    long getCooldownTtlSeconds(String email);
}
