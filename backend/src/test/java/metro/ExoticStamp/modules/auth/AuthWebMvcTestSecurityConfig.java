package metro.ExoticStamp.modules.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@TestConfiguration
@EnableWebSecurity
public class AuthWebMvcTestSecurityConfig {

    @Bean
    public SecurityFilterChain authWebMvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/logout",
                                "/api/v1/auth/logout-all",
                                "/api/v1/auth/change-password"
                        ).authenticated()
                        .anyRequest().permitAll());
        return http.build();
    }
}
