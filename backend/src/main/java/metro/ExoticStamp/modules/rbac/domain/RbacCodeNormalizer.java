package metro.ExoticStamp.modules.rbac.domain;

public final class RbacCodeNormalizer {

    private RbacCodeNormalizer() {}

    public static String normalizeCode(String raw, int maxLength) {
        if (raw == null) {
            throw new IllegalArgumentException("Code must not be null");
        }
        String trimmed = raw.trim().replaceAll("\\s+", "_");
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Code must not be blank");
        }
        String upper = trimmed.toUpperCase();
        if (upper.length() > maxLength) {
            throw new IllegalArgumentException("Code exceeds maximum length");
        }
        if (!RbacValidationConstants.CODE_PATTERN.matcher(upper).matches()) {
            throw new IllegalArgumentException(RbacValidationConstants.CODE_PATTERN_MESSAGE);
        }
        return upper;
    }
}
