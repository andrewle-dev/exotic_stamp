package metro.ExoticStamp.modules.auth.infrastructure.redis;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.common.exceptions.security.SecurityDependencyUnavailableException;
import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.modules.auth.config.AuthSecurityProperties;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRedisRepositoryTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ValueOperations<String, Object> valueOperations;

    private SimpleMeterRegistry meterRegistry;
    private AuthSecurityProperties authProps;
    private CacheProperties cacheProps;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        authProps = new AuthSecurityProperties();
        cacheProps = new CacheProperties();
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void otpRepository_saveAndFind_success() {
        OtpRepository repo = new OtpRepository(redisTemplate, meterRegistry, authProps);
        String email = "user@test.com";
        repo.save(email, OtpType.EMAIL_VERIFY, "123456");
        verify(valueOperations).set(
                eq("auth:otp:email_verify:" + email),
                eq("123456"),
                eq(authProps.getOtp().getEmailVerify().getTtl()));
        when(valueOperations.get("auth:otp:email_verify:" + email)).thenReturn("123456");
        assertEquals(Optional.of("123456"), repo.find(email, OtpType.EMAIL_VERIFY));
    }

    @Test
    void otpRepository_saveRequired_redisFailureThrows() {
        OtpRepository repo = new OtpRepository(redisTemplate, meterRegistry, authProps);
        doThrow(new RuntimeException("redis down")).when(valueOperations)
                .set(anyString(), any(), any(Duration.class));
        assertThrows(SecurityDependencyUnavailableException.class, () ->
                repo.save("a@test.com", OtpType.FORGOT_PASSWORD, "999999"));
    }

    @Test
    void otpRepository_maxAttemptsExceeded_whenAtLimit() {
        OtpRepository repo = new OtpRepository(redisTemplate, meterRegistry, authProps);
        String email = "user@test.com";
        when(valueOperations.get("auth:otp:attempts:email_verify:" + email)).thenReturn("5");
        assertTrue(repo.isMaxAttemptsExceeded(email, OtpType.EMAIL_VERIFY));
    }

    @Test
    void refreshToken_isKnownRevoked_redisErrorReturnsFalse() {
        RefreshTokenRedisRepository repo = new RefreshTokenRedisRepository(redisTemplate, meterRegistry, cacheProps);
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("redis down"));
        assertFalse(repo.isKnownRevoked("hash123"));
        assertFalse(repo.isHealthy());
    }

    @Test
    void accessRevocation_denylistCheck_redisErrorUnavailable() {
        AccessTokenRevocationRedisRepository repo =
                new AccessTokenRevocationRedisRepository(redisTemplate, meterRegistry, cacheProps);
        when(redisTemplate.hasKey(anyString())).thenThrow(new RuntimeException("redis down"));
        assertEquals(AccessTokenRevocationRedisRepository.DenylistCheck.UNAVAILABLE,
                repo.isDenylistedCheck("jti-x"));
    }

    @Test
    void accessRevocation_getCachedTokenVersion_parsesNumber() {
        AccessTokenRevocationRedisRepository repo =
                new AccessTokenRevocationRedisRepository(redisTemplate, meterRegistry, cacheProps);
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("user:" + userId + ":tokenVersion")).thenReturn(42L);
        assertEquals(Optional.of(42L), repo.getCachedTokenVersion(userId));
    }

    @Test
    void accessRevocation_denylistCheck_clearAndDenylisted() {
        AccessTokenRevocationRedisRepository repo =
                new AccessTokenRevocationRedisRepository(redisTemplate, meterRegistry, cacheProps);
        when(redisTemplate.hasKey("denylist:jti-ok")).thenReturn(false);
        when(redisTemplate.hasKey("denylist:jti-bad")).thenReturn(true);

        assertEquals(AccessTokenRevocationRedisRepository.DenylistCheck.CLEAR, repo.isDenylistedCheck("jti-ok"));
        assertEquals(AccessTokenRevocationRedisRepository.DenylistCheck.DENYLISTED, repo.isDenylistedCheck("jti-bad"));
        assertTrue(repo.isDenylisted("jti-bad"));
    }

    @Test
    void accessRevocation_getCachedTokenVersion_parsesStringAndRejectsGarbage() {
        AccessTokenRevocationRedisRepository repo =
                new AccessTokenRevocationRedisRepository(redisTemplate, meterRegistry, cacheProps);
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("user:" + userId + ":tokenVersion")).thenReturn("7");
        assertEquals(Optional.of(7L), repo.getCachedTokenVersion(userId));

        when(valueOperations.get("user:" + userId + ":tokenVersion")).thenReturn("not-a-number");
        assertEquals(Optional.empty(), repo.getCachedTokenVersion(userId));
    }

    @Test
    void accessRevocation_deviceJtiRoundTrip() {
        AccessTokenRevocationRedisRepository repo =
                new AccessTokenRevocationRedisRepository(redisTemplate, meterRegistry, cacheProps);
        UUID userId = UUID.randomUUID();
        String fp = "device-fp";
        when(valueOperations.get("auth:access_jti:" + userId + ":" + fp)).thenReturn("jti-42");

        assertEquals(Optional.of("jti-42"), repo.getDeviceAccessJti(userId, fp));
        repo.deleteDeviceAccessJti(userId, fp);
        verify(redisTemplate).delete("auth:access_jti:" + userId + ":" + fp);
    }

    @Test
    void refreshToken_graceCredentials_roundTripAndMalformed() {
        RefreshTokenRedisRepository repo = new RefreshTokenRedisRepository(redisTemplate, meterRegistry, cacheProps);
        when(valueOperations.get("auth:refresh_token:grace:old-hash"))
                .thenReturn("access-token\nrefresh-token");

        Optional<RefreshTokenRedisRepository.GracePayload> payload = repo.findGraceCredentials("old-hash");
        assertTrue(payload.isPresent());
        assertEquals("access-token", payload.get().accessToken());
        assertEquals("refresh-token", payload.get().refreshToken());

        when(valueOperations.get("auth:refresh_token:grace:bad")).thenReturn("no-separator");
        assertEquals(Optional.empty(), repo.findGraceCredentials("bad"));
    }

    @Test
    void refreshToken_isKnownRevoked_trueWhenPresent() {
        RefreshTokenRedisRepository repo = new RefreshTokenRedisRepository(redisTemplate, meterRegistry, cacheProps);
        when(redisTemplate.hasKey("auth:refresh_token:revoked:hash")).thenReturn(true);
        assertTrue(repo.isKnownRevoked("hash"));
        assertTrue(repo.isHealthy());
    }

    @Test
    void refreshToken_saveAndFindHash() {
        RefreshTokenRedisRepository repo = new RefreshTokenRedisRepository(redisTemplate, meterRegistry, cacheProps);
        UUID userId = UUID.randomUUID();
        when(valueOperations.get("auth:refresh_token:valid:" + userId + ":fp")).thenReturn("hash-1");

        repo.save(userId, "fp", "hash-1");
        verify(valueOperations).set(
                eq("auth:refresh_token:valid:" + userId + ":fp"),
                eq("hash-1"),
                eq(cacheProps.getRefreshTokenTtl()));

        assertEquals(Optional.of("hash-1"), repo.findHash(userId, "fp"));
    }

    @Test
    void accessRevocation_addToDenylistRequired_redisFailureThrows() {
        AccessTokenRevocationRedisRepository repo =
                new AccessTokenRevocationRedisRepository(redisTemplate, meterRegistry, cacheProps);
        doThrow(new RuntimeException("redis down")).when(valueOperations)
                .set(anyString(), any(), any(Duration.class));

        assertThrows(SecurityDependencyUnavailableException.class, () ->
                repo.addToDenylistRequired("jti-1", Duration.ofMinutes(15)));
    }

    @Test
    void accessRevocation_setCachedTokenVersion_writesValue() {
        AccessTokenRevocationRedisRepository repo =
                new AccessTokenRevocationRedisRepository(redisTemplate, meterRegistry, cacheProps);
        UUID userId = UUID.randomUUID();
        repo.setCachedTokenVersion(userId, 9L);
        verify(valueOperations).set(
                eq("user:" + userId + ":tokenVersion"),
                eq(9L),
                eq(cacheProps.getAccessTokenVersionTtl()));
    }
}