package metro.ExoticStamp.modules.auth.infrastructure.jwt;

import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret("0123456789012345678901234567890123456789012345678901234567890123");
        props.setAccessTokenTtl(Duration.ofMinutes(15));
        props.setRefreshTokenTtl(Duration.ofDays(7));
        jwtProvider = new JwtProvider(props);
    }

    @Test
    void issueAccessToken_includesJtiAndTokenVersion() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .firstname("A")
                .lastname("B")
                .username("u1")
                .email("a@b.c")
                .phoneNumber("+1234567890")
                .password("secretsecret")
                .status(UserStatus.ACTIVE)
                .verifiedAt(LocalDateTime.now())
                .tokenVersion(3L)
                .build();
        user.setId(id);

        IssuedAccessToken issued = jwtProvider.issueAccessToken(user, List.of("USER"));
        assertNotNull(issued.jti());
        ParsedAccessToken parsed = jwtProvider.parseAccessToken(issued.token());
        assertEquals(id, parsed.userId());
        assertEquals(issued.jti(), parsed.jti());
        assertEquals(3L, parsed.tokenVersion());
    }

    @Test
    void issueAccessToken_acceptsShortSecretByDerivingKey() {
        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret("short-secret");
        shortProps.setAccessTokenTtl(Duration.ofMinutes(15));
        JwtProvider shortProvider = new JwtProvider(shortProps);

        UUID id = UUID.randomUUID();
        User user = User.builder()
                .firstname("A")
                .lastname("B")
                .username("u2")
                .email("short@b.c")
                .phoneNumber("+1234567891")
                .password("secretsecret")
                .status(UserStatus.ACTIVE)
                .verifiedAt(LocalDateTime.now())
                .tokenVersion(1L)
                .build();
        user.setId(id);

        IssuedAccessToken issued = shortProvider.issueAccessToken(user, List.of("USER"));
        ParsedAccessToken parsed = shortProvider.parseAccessToken(issued.token());
        assertEquals(id, parsed.userId());
    }

    @Test
    void parseAccessToken_rejectsRefreshToken() {
        UUID id = UUID.randomUUID();
        String refresh = jwtProvider.generateRefreshToken(id);
        assertThrows(Exception.class, () -> jwtProvider.parseAccessToken(refresh));
    }
}
