package metro.ExoticStamp.modules.auth.infrastructure.jwt;

import java.util.Base64;

/**
 * Shared JWT secret rules for runtime ({@link JwtProvider}) and prod startup fail-fast.
 */
public final class JwtSecretValidator {

    private static final int MIN_DECODED_BYTES = 32;

    private JwtSecretValidator() {
    }

    /**
     * Validates and Base64-decodes {@code JWT_SECRET} (standard Base64, then URL-safe).
     *
     * @return decoded key bytes (length &gt;= 32)
     * @throws IllegalStateException if blank, malformed Base64, or decoded length &lt; 32
     */
    public static byte[] validateBase64Secret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be a non-blank Base64 string");
        }
        String trimmed = secret.trim();
        byte[] decoded = tryDecode(trimmed);
        if (decoded == null) {
            throw new IllegalStateException("JWT secret must be valid Base64 (standard or URL-safe)");
        }
        if (decoded.length < MIN_DECODED_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must decode to at least " + MIN_DECODED_BYTES + " bytes");
        }
        return decoded;
    }

    private static byte[] tryDecode(String value) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException ignored) {
            // fall through to URL-safe
        }
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
