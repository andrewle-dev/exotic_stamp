package metro.ExoticStamp.infra.security.ratelimit;

import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds composite rate-limit Redis/memory keys without embedding raw PII.
 * <p>
 * HMAC-SHA256 output is truncated to 32 lowercase hex chars (128 bits). Birthday collision
 * probability remains ~2^64, which is acceptable for ephemeral rate-bucket keys (not security MACs).
 */
public class RateLimitKeyHasher {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final int HEX_TRUNCATE_CHARS = 32;

    private final RateLimitProperties properties;

    public RateLimitKeyHasher(RateLimitProperties properties) {
        this.properties = properties;
    }

    /**
     * HMAC-SHA256 of {@code normalizedIdentifier}, lowercase hex truncated to 32 characters (128 bits).
     */
    public String hmacHex(String normalizedIdentifier) {
        requirePepper();
        Objects.requireNonNull(normalizedIdentifier, "normalizedIdentifier");
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(
                    properties.getKeyPepper().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALG));
            byte[] digest = mac.doFinal(normalizedIdentifier.getBytes(StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(digest);
            return hex.substring(0, HEX_TRUNCATE_CHARS);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute rate-limit HMAC", e);
        }
    }

    public String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replace(" ", "").replace("-", "").trim();
    }

    /**
     * Fingerprint for scan payloads / tokens — HMAC of raw value; never store raw.
     */
    public String fingerprintScanKey(String raw) {
        if (raw == null || raw.isBlank()) {
            return hmacHex("");
        }
        return hmacHex(raw.trim());
    }

    /**
     * Composite key: {@code rl:v1:{policy}:{ip}:{hmacPart1:hmacPart2:...}}.
     * Callers must pass already-hashed parts (never raw email/phone/tokens).
     * Rotating {@code RATE_LIMIT_KEY_PEPPER} or bumping the {@code v1} namespace resets active buckets.
     */
    public String buildKey(RateLimitPolicyName policy, String ip, String... hmacParts) {
        String policySegment = policy.name().toLowerCase(Locale.ROOT);
        String ipSegment = (ip == null || ip.isBlank()) ? "unknown" : ip;
        String parts = Stream.of(hmacParts == null ? new String[0] : hmacParts)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(":"));
        if (parts.isEmpty()) {
            return "rl:v1:" + policySegment + ":" + ipSegment;
        }
        return "rl:v1:" + policySegment + ":" + ipSegment + ":" + parts;
    }

    private void requirePepper() {
        if (!StringUtils.hasText(properties.getKeyPepper())) {
            throw new IllegalStateException(
                    "application.security.rate-limit.key-pepper (RATE_LIMIT_KEY_PEPPER) must be set for hashing");
        }
    }
}
