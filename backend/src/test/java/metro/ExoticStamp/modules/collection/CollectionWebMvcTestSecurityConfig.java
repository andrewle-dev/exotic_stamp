package metro.ExoticStamp.modules.collection;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Security rules for {@code @WebMvcTest} on collection controllers.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class CollectionWebMvcTestSecurityConfig {

    @Bean
    public SecurityFilterChain collectionWebMvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/collections/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/collections/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/collection/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/collection/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/campaigns/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/stations/*/campaigns").permitAll()
                        .requestMatchers("/api/v1/admin/collections/**").authenticated()
                        .requestMatchers("/api/v1/admin/campaigns/**").authenticated()
                        .requestMatchers("/api/v1/admin/stamp-designs/**").authenticated()
                        .anyRequest().permitAll());
        return http.build();
    }
}
