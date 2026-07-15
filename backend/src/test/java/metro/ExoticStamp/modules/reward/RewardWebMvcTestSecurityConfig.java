package metro.ExoticStamp.modules.reward;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class RewardWebMvcTestSecurityConfig {

    @Bean
    public SecurityFilterChain rewardWebMvcSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/v1/partners/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/rewards/my/**").authenticated()
                        .anyRequest().authenticated());
        return http.build();
    }
}
