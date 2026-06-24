package metro.ExoticStamp.modules.auth.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthSecurityPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void defaults_areValid() {
        AuthSecurityProperties properties = new AuthSecurityProperties();
        assertTrue(validator.validate(properties).isEmpty());
    }

    @Test
    void invalidOtpLength_isRejected() {
        AuthSecurityProperties properties = new AuthSecurityProperties();
        properties.getOtp().setLength(3);

        Set<ConstraintViolation<AuthSecurityProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidOtpCooldown_isRejected() {
        AuthSecurityProperties properties = new AuthSecurityProperties();
        properties.getOtp().setCooldownTtl(Duration.ofMillis(500));

        Set<ConstraintViolation<AuthSecurityProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidVerificationResendCooldown_isRejected() {
        AuthSecurityProperties properties = new AuthSecurityProperties();
        properties.getVerification().setResendCooldownTtl(Duration.ofMillis(500));

        Set<ConstraintViolation<AuthSecurityProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }
}
