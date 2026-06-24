package metro.ExoticStamp.modules.community.domain.service;

import java.security.SecureRandom;

public final class ReferralCodeGenerator {

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int DEFAULT_LENGTH = 8;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(DEFAULT_LENGTH);
        for (int i = 0; i < DEFAULT_LENGTH; i++) {
            sb.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase();
    }
}
