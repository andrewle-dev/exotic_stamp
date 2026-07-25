package metro.ExoticStamp.config;

import metro.ExoticStamp.modules.collection.infrastructure.bootstrap.CollectionBootstrapper;
import metro.ExoticStamp.modules.metro.infrastructure.seeder.MetroLineSeeder;
import metro.ExoticStamp.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("prod")
@Testcontainers(disabledWithoutDocker = true)
class ProdSeederExclusionIT {

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
    private ApplicationContext applicationContext;

    @Test
    void runtimeSeedersAbsentUnderProd() {
        assertThat(applicationContext.getBeanNamesForType(MetroLineSeeder.class)).isEmpty();
        assertThat(applicationContext.getBeanNamesForType(CollectionBootstrapper.class)).isEmpty();
        assertThat(applicationContext.getBeanNamesForType(AdminSeedBootstrap.class)).isEmpty();
        assertThat(applicationContext.getBeanNamesForType(MvpDemoSeedBootstrap.class)).isEmpty();
    }
}
