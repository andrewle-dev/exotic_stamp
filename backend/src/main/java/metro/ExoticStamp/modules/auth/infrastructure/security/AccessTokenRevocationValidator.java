package metro.ExoticStamp.modules.auth.infrastructure.security;

import metro.ExoticStamp.modules.auth.infrastructure.redis.AccessTokenRevocationRedisRepository;
import metro.ExoticStamp.modules.auth.infrastructure.redis.AccessTokenRevocationRedisRepository.DenylistCheck;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Server-side access token revocation: Redis (denylist + version cache) with DB fallback.
 * On DB read failure, behavior is controlled by {@link TokenRevocationProperties#isFailOpenOnDbError()}.
 * Denylist Redis unavailability is fail-closed ({@link AccessTokenRevocationStatus#DEPENDENCY_UNAVAILABLE}).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccessTokenRevocationValidator {

    private final AccessTokenRevocationRedisRepository redis;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;
    private final TokenRevocationProperties tokenRevocationProperties;

    public AccessTokenRevocationStatus validate(UUID userId, String jti, long jwtTokenVersion) {
        DenylistCheck denylist = redis.isDenylistedCheck(jti);
        if (denylist == DenylistCheck.DENYLISTED) {
            return AccessTokenRevocationStatus.REVOKED;
        }
        if (denylist == DenylistCheck.UNAVAILABLE) {
            meterRegistry.counter("auth.revocation.dependency_unavailable", "reason", "denylist").increment();
            log.warn("[Auth] denylist Redis unavailable userId={}", userId);
            return AccessTokenRevocationStatus.DEPENDENCY_UNAVAILABLE;
        }

        // Soft cache: Redis get errors already map to empty (miss) → DB
        Optional<Long> cached = redis.getCachedTokenVersion(userId);
        if (cached.isPresent()) {
            if (cached.get() != jwtTokenVersion) {
                return AccessTokenRevocationStatus.REVOKED;
            }
            return AccessTokenRevocationStatus.OK;
        }

        return validateAgainstDatabase(userId, jwtTokenVersion);
    }

    private AccessTokenRevocationStatus validateAgainstDatabase(UUID userId, long jwtTokenVersion) {
        try {
            Optional<Long> dbVersion = userRepository.findTokenVersionById(userId);
            if (dbVersion.isEmpty()) {
                return AccessTokenRevocationStatus.REVOKED;
            }
            if (dbVersion.get() != jwtTokenVersion) {
                return AccessTokenRevocationStatus.REVOKED;
            }
            redis.setCachedTokenVersion(userId, dbVersion.get());
            return AccessTokenRevocationStatus.OK;
        } catch (Exception e) {
            if (tokenRevocationProperties.isFailOpenOnDbError()) {
                meterRegistry.counter("auth.revocation.fail_open", "reason", "db_error").increment();
                log.error(
                        "[Auth] fail-open: token_version check skipped userId={} err={}",
                        userId,
                        e.getMessage()
                );
                return AccessTokenRevocationStatus.FAIL_OPEN;
            }
            meterRegistry.counter("auth.revocation.fail_closed", "reason", "db_error").increment();
            log.error(
                    "[Auth] fail-closed: token rejected due to DB error userId={} err={}",
                    userId,
                    e.getMessage()
            );
            return AccessTokenRevocationStatus.REVOKED;
        }
    }
}
