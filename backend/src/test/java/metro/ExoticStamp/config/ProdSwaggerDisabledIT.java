package metro.ExoticStamp.config;

import metro.ExoticStamp.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Testcontainers(disabledWithoutDocker = true)
class ProdSwaggerDisabledIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationTestSupport.registerPostgresAndRedis(registry, postgres, redis);
        IntegrationTestSupport.registerCommonSecrets(registry);
        IntegrationTestSupport.registerProdSite(registry);
    }

    @Autowired
    private Environment environment;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void swaggerDisabledInProdProfile() {
        assertThat(environment.getProperty("springdoc.api-docs.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class)).isFalse();
    }

    @Test
    void swaggerUiNotPubliclyAccessibleUnderProd() throws Exception {
        // Unauthenticated → 401 entry point; denyAll still blocks if somehow authenticated → 403.
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertThat(s).as("swagger-ui must not be public").isIn(401, 403, 404);
                });
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(result -> {
                    int s = result.getResponse().getStatus();
                    assertThat(s).as("api-docs must not be public").isIn(401, 403, 404);
                });
    }
}
