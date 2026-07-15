package metro.ExoticStamp.modules.user.application.port;

import metro.ExoticStamp.modules.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Cross-module account access for auth and other bounded contexts.
 * Prefer this port over importing {@code user.domain.repository.UserRepository}.
 */
public interface UserAccountPort {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phone);

    Optional<Long> findTokenVersionById(UUID id);

    int incrementTokenVersionById(UUID id);
}
