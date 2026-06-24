package metro.ExoticStamp.config;

import metro.ExoticStamp.modules.auth.infrastructure.filter.JwtAuthFilter;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CorsConfigurationTest {

    @Mock private JwtAuthFilter jwtAuthFilter;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private CustomAuthEntryPoint authEntryPoint;
    @Mock private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void cors_allowsPatchMethod() {
        CorsProperties corsProperties = devCorsProperties();
        SecurityConfig config = new SecurityConfig(
                jwtAuthFilter, userDetailsService, authEntryPoint, accessDeniedHandler, corsProperties);
        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/v1/users/me"));
        assertNotNull(cors);
        assertTrue(cors.getAllowedMethods().contains("PATCH"));
    }

    @Test
    void cors_includesConfiguredOrigin() {
        CorsProperties corsProperties = devCorsProperties();
        SecurityConfig config = new SecurityConfig(
                jwtAuthFilter, userDetailsService, authEntryPoint, accessDeniedHandler, corsProperties);
        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/v1/users/me"));
        assertNotNull(cors);
        assertTrue(cors.getAllowedOrigins().contains("http://localhost:3000"));
    }

    @Test
    void cors_excludesUnknownOrigin() {
        CorsProperties corsProperties = devCorsProperties();
        SecurityConfig config = new SecurityConfig(
                jwtAuthFilter, userDetailsService, authEntryPoint, accessDeniedHandler, corsProperties);
        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/v1/users/me"));
        assertNotNull(cors);
        assertFalse(cors.getAllowedOrigins().contains("https://evil.example.com"));
    }

    @Test
    void cors_prodLikeOriginsFromProperty() {
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setAllowedOrigins("https://app.exoticstamp.example");
        corsProperties.setAllowedMethods("GET,POST,PATCH");
        corsProperties.setAllowedHeaders("Authorization,Content-Type");
        corsProperties.setAllowCredentials(true);

        SecurityConfig config = new SecurityConfig(
                jwtAuthFilter, userDetailsService, authEntryPoint, accessDeniedHandler, corsProperties);
        CorsConfiguration cors = config.corsConfigurationSource().getCorsConfiguration(
                new org.springframework.mock.web.MockHttpServletRequest("OPTIONS", "/api/v1/users/me"));
        assertNotNull(cors);
        assertTrue(cors.getAllowedOrigins().contains("https://app.exoticstamp.example"));
        assertTrue(cors.getAllowedMethods().contains("PATCH"));
    }

    private static CorsProperties devCorsProperties() {
        CorsProperties props = new CorsProperties();
        props.setAllowedOrigins("http://localhost:3000,http://localhost:5173");
        props.setAllowedMethods("GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD");
        props.setAllowedHeaders("*");
        props.setAllowCredentials(true);
        return props;
    }
}
