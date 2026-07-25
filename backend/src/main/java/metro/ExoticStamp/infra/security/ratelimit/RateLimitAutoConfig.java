package metro.ExoticStamp.infra.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.infra.security.ClientIpResolver;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitAutoConfig {

    @Bean
    public RateLimitKeyHasher rateLimitKeyHasher(RateLimitProperties properties) {
        return new RateLimitKeyHasher(properties);
    }

    @Bean
    public RateLimitService rateLimitService(RateLimitProperties properties, RateLimiter rateLimiter) {
        return new RateLimitService(properties, rateLimiter);
    }

    @Bean
    public RateLimitFilter rateLimitFilter(
            RateLimitProperties properties,
            RateLimitService rateLimitService,
            RateLimitKeyHasher keyHasher,
            ClientIpResolver clientIpResolver,
            ObjectMapper objectMapper
    ) {
        return new RateLimitFilter(properties, rateLimitService, keyHasher, clientIpResolver, objectMapper);
    }

    /**
     * Disable servlet-container registration; filter runs only in the Security filter chain.
     */
    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter filter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
