package metro.ExoticStamp.modules.auth.domain.repository;

import metro.ExoticStamp.modules.auth.domain.model.AccessToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessTokenRepository {
    AccessToken save(AccessToken token);

    Optional<AccessToken> findByTokenHash(String hash);

    List<AccessToken> findAllActiveByUserId(UUID userId);

    void revokeByTokenHash(String hash, String reason);

    void revokeAllByUserId(UUID userId, String reason);

    boolean existsByTokenHash(String hash);
}

