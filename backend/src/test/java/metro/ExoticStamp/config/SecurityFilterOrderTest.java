package metro.ExoticStamp.config;

import metro.ExoticStamp.modules.auth.infrastructure.filter.JwtAuthFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit-level swagger gate. Full filter order (Jwt then RateLimit) is proven by
 * {@code SecurityFilterChainIT}.
 */
@ExtendWith(MockitoExtension.class)
class SecurityFilterOrderTest {

    @Mock private JwtAuthFilter jwtAuthFilter;
    @Mock private Environment environment;

    @Test
    void swaggerDeniedOnProdEvenIfDocsPropertyTrue() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        SecurityConfig config = new SecurityConfig(
                jwtAuthFilter, null, null, null,
                new CorsProperties(), null, null, environment);
        ReflectionTestUtils.setField(config, "springdocApiDocsEnabled", true);
        assertThat(config.isSwaggerDocsAllowed()).isFalse();
    }

    @Test
    void swaggerAllowedWhenDocsEnabledAndNonProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[]{"dev"});
        SecurityConfig config = new SecurityConfig(
                jwtAuthFilter, null, null, null,
                new CorsProperties(), null, null, environment);
        ReflectionTestUtils.setField(config, "springdocApiDocsEnabled", true);
        assertThat(config.isSwaggerDocsAllowed()).isTrue();
    }
}
