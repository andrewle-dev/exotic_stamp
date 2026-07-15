package metro.ExoticStamp.modules.mobile.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobileAppConfigPropertiesTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void defaults_areValid() {
        MobileAppConfigProperties properties = new MobileAppConfigProperties();
        assertTrue(validator.validate(properties).isEmpty());
    }

    @Test
    void blankMinimumSupportedVersion_isRejected() {
        MobileAppConfigProperties properties = new MobileAppConfigProperties();
        properties.getAndroid().setMinimumSupportedVersion("  ");

        Set<ConstraintViolation<MobileAppConfigProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankLatestVersion_isRejected() {
        MobileAppConfigProperties properties = new MobileAppConfigProperties();
        properties.getIos().setLatestVersion("");

        Set<ConstraintViolation<MobileAppConfigProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
    }
}
