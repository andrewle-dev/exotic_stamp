package metro.ExoticStamp.modules.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthCookiePropertiesTest {

    @Test
    void prodProfile_requiresSecureAlways() {
        AuthCookieProperties props = new AuthCookieProperties();
        props.setSecureAlways(false);
        ReflectionTestUtils.setField(props, "activeProfiles", "prod");
        assertThrows(IllegalStateException.class, props::requireSecureAlwaysInProduction);
    }

    @Test
    void prodProfile_withSecureAlways_ok() {
        AuthCookieProperties props = new AuthCookieProperties();
        props.setSecureAlways(true);
        ReflectionTestUtils.setField(props, "activeProfiles", "prod");
        assertDoesNotThrow(props::requireSecureAlwaysInProduction);
    }

    @Test
    void devProfile_allowsSecureAlwaysFalse() {
        AuthCookieProperties props = new AuthCookieProperties();
        props.setSecureAlways(false);
        ReflectionTestUtils.setField(props, "activeProfiles", "dev");
        assertDoesNotThrow(props::requireSecureAlwaysInProduction);
    }
}
