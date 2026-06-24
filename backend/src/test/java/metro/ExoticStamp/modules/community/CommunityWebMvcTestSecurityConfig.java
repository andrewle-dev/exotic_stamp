package metro.ExoticStamp.modules.community;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
public class CommunityWebMvcTestSecurityConfig {

    @Bean
    public SecurityFilterChain communityWebMvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/community/**", "/api/v1/notifications/**").authenticated()
                        .anyRequest().authenticated());
        return http.build();
    }
}
