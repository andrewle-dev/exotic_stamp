package metro.ExoticStamp.modules.user.domain.repository;

import metro.ExoticStamp.common.model.PageQuery;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/** === DO NOT IMPORT SPRING/JPA - ONLY IMPORT FROM DOMAIN/MODEL === */
public interface UserRepository {
    User             save(User user);
    Optional<User>   findById(UUID id);
    Optional<User>   findByEmail(String email);
    Optional<User>   findByUsername(String username);
    boolean          existsByEmail(String email);
    boolean          existsByUsername(String username);
    boolean          existsByPhoneNumber(String phone);
    boolean          existsById(UUID id);
    void             deleteById(UUID id);
    PageResult<User> findAll(PageQuery query);

    /** Authoritative token generation counter for access JWT validation. */
    Optional<Long> findTokenVersionById(UUID id);

    /** Increments token version; clears persistence context so a follow-up read sees the new value. */
    int incrementTokenVersionById(UUID id);
}
