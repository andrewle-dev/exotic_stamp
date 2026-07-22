package metro.ExoticStamp.modules.auth.domain.repository;

import metro.ExoticStamp.modules.auth.domain.model.AccessToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessTokenRepository {
    AccessToken save(AccessToken token);

    Optional<AccessToken> findByTokenHash(String hash);

    /** Pessimistic write lock for atomic rotation. */
    Optional<AccessToken> findByTokenHashForUpdate(String hash);

    Optional<AccessToken> findById(UUID id);

    List<AccessToken> findAllActiveByUserId(UUID userId);

    void revokeByTokenHash(String hash, String reason);

    void revokeAllByUserId(UUID userId, String reason);

    void revokeAllByFamilyId(UUID familyId, String reason);

    boolean existsByTokenHash(String hash);
}
