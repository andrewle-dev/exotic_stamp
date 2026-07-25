package metro.ExoticStamp.modules.collection.application.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/**
 * Stable logical-request fingerprint for collect idempotency (R-P1-03 / Batch E.1).
 *
 * <p>Canonical serialization (pipe-delimited, no JSON):
 * {@code COLLECT|{userId}|{stationId}|{campaignId}|{scanType}}
 * where {@code scanType} is upper-case ASCII.
 *
 * <p>Excludes timestamps, correlation IDs, GPS coordinates, device metadata, and raw NFC/QR payloads.
 * Hash is SHA-256 hex (64 chars).
 */
public final class CollectIdempotencyFingerprint {

    public static final String OPERATION = "COLLECT";

    private CollectIdempotencyFingerprint() {
    }

    public static String compute(UUID userId, UUID stationId, UUID campaignId, String scanType) {
        if (userId == null || stationId == null || campaignId == null) {
            throw new IllegalArgumentException("userId, stationId, and campaignId are required for fingerprint");
        }
        String type = scanType == null ? "" : scanType.trim().toUpperCase(Locale.ROOT);
        String canonical = OPERATION + "|" + userId + "|" + stationId + "|" + campaignId + "|" + type;
        return sha256Hex(canonical);
    }

    public static boolean matches(String stored, String computed) {
        if (stored == null || computed == null) {
            return false;
        }
        return stored.equalsIgnoreCase(computed);
    }

    private static String sha256Hex(String canonical) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
