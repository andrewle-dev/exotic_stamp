package metro.ExoticStamp.modules.auth.infrastructure.persistence;

import metro.ExoticStamp.modules.auth.domain.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaAccessTokenRepository extends JpaRepository<AccessToken, UUID> {

    Optional<AccessToken> findByTokenHash(String hash);

    @Query("SELECT t FROM AccessToken t WHERE t.userId = :userId " +
            "AND t.revokedAt IS NULL AND t.expiresAt > :now")
    List<AccessToken> findAllActiveByUserId(@Param("userId") UUID userId,
                                             @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AccessToken t SET t.revokedAt = :now, t.revokedReason = :reason " +
            "WHERE t.tokenHash = :hash")
    void revokeByTokenHash(@Param("hash") String hash,
                            @Param("reason") String reason,
                            @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE AccessToken t SET t.revokedAt = :now, t.revokedReason = :reason " +
            "WHERE t.userId = :userId AND t.revokedAt IS NULL")
    void revokeAllByUserId(@Param("userId") UUID userId,
                            @Param("reason") String reason,
                            @Param("now") LocalDateTime now);
}

