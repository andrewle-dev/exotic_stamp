package metro.ExoticStamp;

import metro.ExoticStamp.infra.security.ratelimit.RateLimitFilter;
import metro.ExoticStamp.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-context proof that the production security chain boots with the real
 * {@link RateLimitFilter} (no test-only exclusion).
 */
@SpringBootTest
@ActiveProfiles("dev")
@Testcontainers(disabledWithoutDocker = true)
class SecurityFilterChainIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationTestSupport.registerPostgresAndRedis(registry, postgres, redis);
        IntegrationTestSupport.registerCommonSecrets(registry);
        IntegrationTestSupport.registerDevBootstrap(registry);
        registry.add("application.security.rate-limit.backend", () -> "redis");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Test
    void contextLoadsWithRealRateLimitFilter() {
        assertThat(applicationContext.getBean(RateLimitFilter.class)).isSameAs(rateLimitFilter);
        FilterChainProxy proxy = applicationContext.getBean(FilterChainProxy.class);
        assertThat(proxy.getFilterChains()).isNotEmpty();
        SecurityFilterChain chain = proxy.getFilterChains().get(0);
        assertThat(chain.getFilters()).anyMatch(f -> f == rateLimitFilter);
        assertThat(chain.getFilters().stream()
                .filter(f -> f.getClass().getSimpleName().equals("JwtAuthFilter"))
                .count()).isEqualTo(1L);
        assertThat(chain.getFilters().stream()
                .filter(f -> f instanceof RateLimitFilter)
                .count()).isEqualTo(1L);

        int jwtIndex = -1;
        int rlIndex = -1;
        var filters = chain.getFilters();
        for (int i = 0; i < filters.size(); i++) {
            if (filters.get(i).getClass().getSimpleName().equals("JwtAuthFilter")) {
                jwtIndex = i;
            }
            if (filters.get(i) instanceof RateLimitFilter) {
                rlIndex = i;
            }
        }
        assertThat(jwtIndex).isGreaterThanOrEqualTo(0);
        assertThat(rlIndex).isGreaterThan(jwtIndex);
    }
}
