package metro.ExoticStamp.modules.auth.infrastructure.jwt;

import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.user.domain.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import static java.util.Base64.getUrlEncoder;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_TYPE = "tokenType";
    static final String CLAIM_TOKEN_VERSION = "tokenVersion";

    static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final JwtProperties props;

    /**
     * Issues an access token with {@code jti} and {@code tokenVersion} for server-side revocation.
     */
    public IssuedAccessToken issueAccessToken(User user, List<String> roles) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiry = Date.from(Instant.now().plus(props.getAccessTokenTtl()));

        String compact = Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .id(jti)
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_TOKEN_VERSION, user.getTokenVersion())
                .signWith(secretKey())
                .compact();
        return new IssuedAccessToken(compact, jti);
    }

    /**
     * Parses and validates an access JWT (signature, expiry, type, {@code jti}, {@code tokenVersion}).
     */
    public ParsedAccessToken parseAccessToken(String token) {
        Claims claims = extractClaims(token);
        if (!TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new InvalidTokenException("Not an access token");
        }
        String jti = claims.getId();
        if (jti == null || jti.isBlank()) {
            throw new InvalidTokenException("Missing jti");
        }
        Object rawVersion = claims.get(CLAIM_TOKEN_VERSION);
        if (rawVersion == null) {
            throw new InvalidTokenException("Missing tokenVersion");
        }
        long tokenVersion = rawVersion instanceof Number n ? n.longValue() : Long.parseLong(rawVersion.toString());
        UUID userId = UUID.fromString(claims.getSubject());
        return new ParsedAccessToken(userId, jti, tokenVersion);
    }

    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiry = Date.from(Instant.now().plus(props.getRefreshTokenTtl()));

        return Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .signWith(secretKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException(e.getMessage());
        } catch (JwtException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).getSubject());
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String hashToken(String token) {
        byte[] bytes = token.getBytes(StandardCharsets.UTF_8);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
        byte[] hashBytes = digest.digest(bytes);
        return getUrlEncoder().withoutPadding().encodeToString(hashBytes);
    }

    private SecretKey secretKey() {
        byte[] secretBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            try {
                secretBytes = MessageDigest.getInstance("SHA-256").digest(secretBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 not available", e);
            }
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }
}

