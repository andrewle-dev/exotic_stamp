package metro.ExoticStamp.modules.metro.application.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class ScanKeyHasher {

    private final String hashSecret;

    public ScanKeyHasher(
            @Value("${metro.scan-key.hash-secret:}") String hashSecret) {
        this.hashSecret = hashSecret == null || hashSecret.isBlank() ? null : hashSecret;
    }

    public String hash(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalArgumentException("rawKey must not be blank");
        }
        byte[] input = rawKey.getBytes(StandardCharsets.UTF_8);
        if (hashSecret != null) {
            return hmacSha256Hex(input, hashSecret);
        }
        return sha256Hex(input);
    }

    private static String sha256Hex(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(input));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String hmacSha256Hex(byte[] input, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(input));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }
}
