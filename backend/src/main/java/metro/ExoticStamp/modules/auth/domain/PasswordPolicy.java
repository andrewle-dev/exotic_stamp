package metro.ExoticStamp.modules.auth.domain;

import metro.ExoticStamp.modules.auth.domain.exception.PasswordPolicyViolationException;

/**
 * Shared plaintext password rules for registration, reset-password, and change-password.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 50;

    private PasswordPolicy() {
    }

    public static void validatePlaintext(String password) {
        if (password == null || password.isBlank()) {
            throw new PasswordPolicyViolationException("Password must not be blank");
        }
        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new PasswordPolicyViolationException(
                    "Password must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters"
            );
        }
    }
}
