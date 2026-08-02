package metro.ExoticStamp.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CorsPropertiesWildcardTest {

    @Test
    void rejectWildcardOriginsWithCredentials() {
        CorsProperties props = new CorsProperties();
        props.setAllowCredentials(true);
        props.setAllowedOrigins("*");
        assertThatThrownBy(props::rejectWildcardOriginsWithCredentials)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("allowCredentials");
    }

    @Test
    void allowsExplicitOriginsWithCredentials() {
        CorsProperties props = new CorsProperties();
        props.setAllowCredentials(true);
        props.setAllowedOrigins("https://admin.example.com");
        assertDoesNotThrow(props::rejectWildcardOriginsWithCredentials);
    }

    @Test
    void allowsWildcardWhenCredentialsDisabled() {
        CorsProperties props = new CorsProperties();
        props.setAllowCredentials(false);
        props.setAllowedOrigins("*");
        assertDoesNotThrow(props::rejectWildcardOriginsWithCredentials);
    }
}
