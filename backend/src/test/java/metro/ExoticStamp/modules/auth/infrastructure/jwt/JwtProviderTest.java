package metro.ExoticStamp.modules.auth.infrastructure.jwt;

import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtProviderTest {

    private JwtProperties props;
    private JwtProvider jwtProvider;
    private String base64Secret;

    @BeforeEach
    void setUp() {
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        base64Secret = Base64.getEncoder().encodeToString(raw);

        props = new JwtProperties();
        props.setSecret(base64Secret);
        props.setAccessTokenTtl(Duration.ofMinutes(15));
        props.setRefreshTokenTtl(Duration.ofDays(7));
        props.setIssuer("exotic-stamp");
        props.setClockSkewSeconds(30);
        jwtProvider = new JwtProvider(props);
    }

    @Test
    void issueAccessToken_includesJtiAndTokenVersion() {
        UUID id = UUID.randomUUID();
        User user = sampleUser(id, 3L);

        IssuedAccessToken issued = jwtProvider.issueAccessToken(user, List.of("USER"));
        assertNotNull(issued.jti());
        ParsedAccessToken parsed = jwtProvider.parseAccessToken(issued.token());
        assertEquals(id, parsed.userId());
        assertEquals(issued.jti(), parsed.jti());
        assertEquals(3L, parsed.tokenVersion());
    }

    @Test
    void rejectsShortDecodedSecret() {
        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret(Base64.getEncoder().encodeToString(new byte[16]));
        shortProps.setAccessTokenTtl(Duration.ofMinutes(15));
        JwtProvider shortProvider = new JwtProvider(shortProps);

        assertThrows(IllegalStateException.class,
                () -> shortProvider.issueAccessToken(sampleUser(UUID.randomUUID(), 1L), List.of("USER")));
    }

    @Test
    void rejectsMalformedBase64() {
        JwtProperties bad = new JwtProperties();
        bad.setSecret("not-valid-base64!!!");
        bad.setAccessTokenTtl(Duration.ofMinutes(15));
        JwtProvider badProvider = new JwtProvider(bad);

        assertThrows(IllegalStateException.class,
                () -> badProvider.issueAccessToken(sampleUser(UUID.randomUUID(), 1L), List.of("USER")));
    }

    @Test
    void acceptsUrlSafeBase64Secret() {
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        String urlSafe = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        JwtProperties urlProps = new JwtProperties();
        urlProps.setSecret(urlSafe);
        urlProps.setAccessTokenTtl(Duration.ofMinutes(15));
        urlProps.setIssuer("exotic-stamp");
        JwtProvider urlProvider = new JwtProvider(urlProps);

        UUID id = UUID.randomUUID();
        IssuedAccessToken issued = urlProvider.issueAccessToken(sampleUser(id, 1L), List.of("USER"));
        assertEquals(id, urlProvider.parseAccessToken(issued.token()).userId());
    }

    @Test
    void parseAccessToken_rejectsRefreshToken() {
        UUID id = UUID.randomUUID();
        String refresh = jwtProvider.generateRefreshToken(id);
        assertThrows(InvalidTokenException.class, () -> jwtProvider.parseAccessToken(refresh));
    }

    @Test
    void parseRefreshToken_rejectsAccessToken() {
        IssuedAccessToken access = jwtProvider.issueAccessToken(
                sampleUser(UUID.randomUUID(), 1L), List.of("USER"));
        assertThrows(InvalidTokenException.class, () -> jwtProvider.parseRefreshToken(access.token()));
    }

    @Test
    void parseRefreshToken_acceptsRefresh() {
        UUID id = UUID.randomUUID();
        String refresh = jwtProvider.generateRefreshToken(id);
        assertEquals(id, jwtProvider.parseRefreshToken(refresh));
        assertTrue(jwtProvider.isTokenValid(refresh));
        assertFalse(jwtProvider.isTokenValid(
                jwtProvider.issueAccessToken(sampleUser(id, 1L), List.of("USER")).token()));
    }

    @Test
    void extractClaims_rejectsWrongIssuer() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(base64Secret));
        String forged = Jwts.builder()
                .issuer("evil-issuer")
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .claim("tokenType", "ACCESS")
                .id(UUID.randomUUID().toString())
                .claim("tokenVersion", 1L)
                .signWith(key)
                .compact();

        assertThrows(InvalidTokenException.class, () -> jwtProvider.parseAccessToken(forged));
    }

    @Test
    void extractClaims_rejectsExpired() {
        props.setAccessTokenTtl(Duration.ofMillis(1));
        props.setClockSkewSeconds(0);
        JwtProvider shortLived = new JwtProvider(props);
        IssuedAccessToken issued = shortLived.issueAccessToken(
                sampleUser(UUID.randomUUID(), 1L), List.of("USER"));
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertThrows(TokenExpiredException.class, () -> shortLived.parseAccessToken(issued.token()));
    }

    @Test
    void extractClaims_rejectsMalformedJwt() {
        assertThrows(InvalidTokenException.class, () -> jwtProvider.extractClaims("not.a.jwt"));
    }

    @Test
    void extractClaims_rejectsUnsigned() {
        String unsigned = Jwts.builder()
                .issuer(props.getIssuer())
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .claim("tokenType", "ACCESS")
                .id(UUID.randomUUID().toString())
                .claim("tokenVersion", 1L)
                .compact();
        assertThrows(InvalidTokenException.class, () -> jwtProvider.parseAccessToken(unsigned));
    }

    @Test
    void parseAccessToken_rejectsMissingJti() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(base64Secret));
        String token = Jwts.builder()
                .issuer(props.getIssuer())
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .claim("tokenType", "ACCESS")
                .claim("tokenVersion", 1L)
                .signWith(key)
                .compact();
        assertThrows(InvalidTokenException.class, () -> jwtProvider.parseAccessToken(token));
    }

    @Test
    void parseAccessToken_rejectsMissingTokenVersion() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(base64Secret));
        String token = Jwts.builder()
                .issuer(props.getIssuer())
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .claim("tokenType", "ACCESS")
                .id(UUID.randomUUID().toString())
                .signWith(key)
                .compact();
        assertThrows(InvalidTokenException.class, () -> jwtProvider.parseAccessToken(token));
    }

    @Test
    void clockSkew_allowsTokenWithinSkewWindow() {
        SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                Base64.getDecoder().decode(base64Secret));
        Instant now = Instant.now();
        String token = Jwts.builder()
                .issuer(props.getIssuer())
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(now.minusSeconds(20)))
                .notBefore(Date.from(now.minusSeconds(20)))
                .expiration(Date.from(now.plusSeconds(60)))
                .claim("tokenType", "ACCESS")
                .id(UUID.randomUUID().toString())
                .claim("tokenVersion", 1L)
                .signWith(key)
                .compact();
        assertNotNull(jwtProvider.parseAccessToken(token));
    }

    @Test
    void jwtSecretValidator_rejectsShortAndMalformed() {
        assertThrows(IllegalStateException.class,
                () -> JwtSecretValidator.validateBase64Secret("%%%"));
        assertThrows(IllegalStateException.class,
                () -> JwtSecretValidator.validateBase64Secret(
                        Base64.getEncoder().encodeToString(new byte[8])));
        assertNotNull(JwtSecretValidator.validateBase64Secret(base64Secret));
    }

    private static User sampleUser(UUID id, long tokenVersion) {
        User user = User.builder()
                .firstname("A")
                .lastname("B")
                .username("u1")
                .email("a@b.c")
                .phoneNumber("+1234567890")
                .password("secretsecret")
                .status(UserStatus.ACTIVE)
                .verifiedAt(LocalDateTime.now())
                .tokenVersion(tokenVersion)
                .build();
        user.setId(id);
        return user;
    }
}
