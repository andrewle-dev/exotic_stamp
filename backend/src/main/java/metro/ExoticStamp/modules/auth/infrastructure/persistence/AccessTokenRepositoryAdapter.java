package metro.ExoticStamp.modules.auth.infrastructure.persistence;

import metro.ExoticStamp.modules.auth.domain.model.AccessToken;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenRepositoryAdapter implements AccessTokenRepository {

    private final JpaAccessTokenRepository jpa;

    @Override
    public AccessToken save(AccessToken token) {
        return jpa.save(token);
    }

    @Override
    public Optional<AccessToken> findByTokenHash(String hash) {
        return jpa.findByTokenHash(hash);
    }

    @Override
    public Optional<AccessToken> findByTokenHashForUpdate(String hash) {
        return jpa.findByTokenHashForUpdate(hash);
    }

    @Override
    public Optional<AccessToken> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<AccessToken> findAllActiveByUserId(UUID userId) {
        return jpa.findAllActiveByUserId(userId, LocalDateTime.now());
    }

    @Override
    public void revokeByTokenHash(String hash, String reason) {
        jpa.revokeByTokenHashIfActive(hash, reason, LocalDateTime.now());
    }

    @Override
    public void revokeAllByUserId(UUID userId, String reason) {
        jpa.revokeAllByUserId(userId, reason, LocalDateTime.now());
    }

    @Override
    public void revokeAllByFamilyId(UUID familyId, String reason) {
        jpa.revokeAllByFamilyId(familyId, reason, LocalDateTime.now());
    }

    @Override
    public boolean existsByTokenHash(String hash) {
        return jpa.findByTokenHash(hash).isPresent();
    }
}
