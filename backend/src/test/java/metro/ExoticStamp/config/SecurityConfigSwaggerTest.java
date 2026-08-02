package metro.ExoticStamp.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigSwaggerTest {

    @Mock
    private Environment environment;

    @Test
    void swaggerAllowed_whenDocsEnabledAndNonProd() {
        SecurityConfig config = newSecurityConfig(true, new String[]{"dev"});
        assertTrue(config.isSwaggerDocsAllowed());
    }

    @Test
    void swaggerDenied_whenProdProfileEvenIfDocsEnabled() {
        SecurityConfig config = newSecurityConfig(true, new String[]{"prod"});
        assertFalse(config.isSwaggerDocsAllowed());
    }

    @Test
    void swaggerDenied_whenDocsDisabled() {
        SecurityConfig config = newSecurityConfig(false, new String[]{"dev"});
        assertFalse(config.isSwaggerDocsAllowed());
    }

    @Test
    void swaggerDenied_whenProdAndDocsDisabled() {
        SecurityConfig config = newSecurityConfig(false, new String[]{"prod"});
        assertFalse(config.isSwaggerDocsAllowed());
    }

    private SecurityConfig newSecurityConfig(boolean docsEnabled, String[] profiles) {
        when(environment.getActiveProfiles()).thenReturn(profiles);
        SecurityConfig config = new SecurityConfig(
                null, null, null, null, null, null, null, environment);
        ReflectionTestUtils.setField(config, "springdocApiDocsEnabled", docsEnabled);
        return config;
    }
}
