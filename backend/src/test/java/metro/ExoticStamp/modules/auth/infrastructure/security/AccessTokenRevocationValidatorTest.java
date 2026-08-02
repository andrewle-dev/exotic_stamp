package metro.ExoticStamp.modules.auth.infrastructure.security;

import metro.ExoticStamp.modules.auth.infrastructure.redis.AccessTokenRevocationRedisRepository;
import metro.ExoticStamp.modules.auth.infrastructure.redis.AccessTokenRevocationRedisRepository.DenylistCheck;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenRevocationValidatorTest {

    @Mock
    private AccessTokenRevocationRedisRepository redis;

    @Mock
    private UserRepository userRepository;

    private TokenRevocationProperties tokenRevocationProperties;

    private AccessTokenRevocationValidator validator;

    private final UUID userId = UUID.randomUUID();
    private final String jti = "jti-1";

    @BeforeEach
    void setUp() {
        tokenRevocationProperties = new TokenRevocationProperties();
        validator = new AccessTokenRevocationValidator(
                redis, userRepository, new SimpleMeterRegistry(), tokenRevocationProperties);
    }

    @Test
    void denylisted_returnsRevoked() {
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.DENYLISTED);

        assertEquals(AccessTokenRevocationStatus.REVOKED, validator.validate(userId, jti, 0L));
        verify(userRepository, never()).findTokenVersionById(any());
    }

    @Test
    void denylistUnavailable_returnsDependencyUnavailable() {
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.UNAVAILABLE);

        assertEquals(AccessTokenRevocationStatus.DEPENDENCY_UNAVAILABLE, validator.validate(userId, jti, 0L));
        verify(userRepository, never()).findTokenVersionById(any());
    }

    @Test
    void cachedVersionMismatch_afterGlobalRevocation_rejectsAccessFromLosingRace() {
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.CLEAR);
        when(redis.getCachedTokenVersion(userId)).thenReturn(Optional.of(5L));

        assertEquals(AccessTokenRevocationStatus.REVOKED, validator.validate(userId, jti, 4L));
    }

    @Test
    void cachedVersionMatch_returnsOk() {
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.CLEAR);
        when(redis.getCachedTokenVersion(userId)).thenReturn(Optional.of(4L));

        assertEquals(AccessTokenRevocationStatus.OK, validator.validate(userId, jti, 4L));
        verify(userRepository, never()).findTokenVersionById(any());
    }

    @Test
    void cacheMiss_dbMatch_returnsOkAndSetsCache() {
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.CLEAR);
        when(redis.getCachedTokenVersion(userId)).thenReturn(Optional.empty());
        when(userRepository.findTokenVersionById(userId)).thenReturn(Optional.of(7L));

        assertEquals(AccessTokenRevocationStatus.OK, validator.validate(userId, jti, 7L));

        verify(redis).setCachedTokenVersion(userId, 7L);
    }

    @Test
    void cacheMiss_dbMismatch_returnsRevoked() {
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.CLEAR);
        when(redis.getCachedTokenVersion(userId)).thenReturn(Optional.empty());
        when(userRepository.findTokenVersionById(userId)).thenReturn(Optional.of(8L));

        assertEquals(AccessTokenRevocationStatus.REVOKED, validator.validate(userId, jti, 7L));
        verify(redis, never()).setCachedTokenVersion(eq(userId), anyLong());
    }

    @Test
    void dbError_failOpenEnabled_returnsFailOpen() {
        tokenRevocationProperties.setFailOpenOnDbError(true);
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.CLEAR);
        when(redis.getCachedTokenVersion(userId)).thenReturn(Optional.empty());
        when(userRepository.findTokenVersionById(userId)).thenThrow(new RuntimeException("db down"));

        assertEquals(AccessTokenRevocationStatus.FAIL_OPEN, validator.validate(userId, jti, 7L));
    }

    @Test
    void cacheMiss_userNotFoundInDb_returnsRevoked() {
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.CLEAR);
        when(redis.getCachedTokenVersion(userId)).thenReturn(Optional.empty());
        when(userRepository.findTokenVersionById(userId)).thenReturn(Optional.empty());

        assertEquals(AccessTokenRevocationStatus.REVOKED, validator.validate(userId, jti, 7L));
        verify(redis, never()).setCachedTokenVersion(eq(userId), anyLong());
    }

    @Test
    void dbError_failClosed_returnsRevoked() {
        tokenRevocationProperties.setFailOpenOnDbError(false);
        when(redis.isDenylistedCheck(jti)).thenReturn(DenylistCheck.CLEAR);
        when(redis.getCachedTokenVersion(userId)).thenReturn(Optional.empty());
        when(userRepository.findTokenVersionById(userId)).thenThrow(new RuntimeException("db down"));

        assertEquals(AccessTokenRevocationStatus.REVOKED, validator.validate(userId, jti, 7L));
    }
}
