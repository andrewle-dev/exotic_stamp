package metro.ExoticStamp.modules.metro.application.support;

/**
 * Masks scan payloads/keys for logs and audit trails.
 * Never logs the full raw key.
 */
public final class ScanKeyRedactor {

    private static final int VISIBLE_PREFIX = 8;

    private ScanKeyRedactor() {
    }

    /**
     * Masks a raw key or full payload, e.g. {@code nfc_abcd****} or {@code metrostamp://scan?k=nfc_abcd****}.
     */
    public static String redact(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        int keyStart = trimmed.indexOf("k=");
        if (keyStart >= 0 && keyStart + 2 < trimmed.length()) {
            String prefix = trimmed.substring(0, keyStart + 2);
            return prefix + maskKey(trimmed.substring(keyStart + 2));
        }
        return maskKey(trimmed);
    }

    private static String maskKey(String key) {
        if (key.length() <= VISIBLE_PREFIX) {
            return key.charAt(0) + "****";
        }
        return key.substring(0, VISIBLE_PREFIX) + "****";
    }
}
