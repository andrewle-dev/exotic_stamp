package metro.ExoticStamp.config;

import metro.ExoticStamp.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Testcontainers(disabledWithoutDocker = true)
class UploadSecurityIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationTestSupport.registerPostgresAndRedis(registry, postgres, redis);
        IntegrationTestSupport.registerCommonSecrets(registry);
        IntegrationTestSupport.registerDevBootstrap(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicUploadPathDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/uploads/public/sample.png"))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonPublicUploadPathRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/uploads/stations/sample.png"))
                .andExpect(status().isUnauthorized());
    }
}
