package metro.ExoticStamp.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Documents / asserts actuator health probe groups without booting a full Spring context.
 */
class HealthGroupsTest {

    @Test
    void applicationYml_definesLivenessAndReadinessGroups() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application.yml"));
        Properties props = factory.getObject();
        assertThat(props).isNotNull();

        assertThat(props.getProperty("management.endpoint.health.probes.enabled")).isEqualTo("true");
        assertThat(props.getProperty("management.endpoint.health.group.liveness.include"))
                .isEqualTo("livenessState");
        assertThat(props.getProperty("management.endpoint.health.group.readiness.include"))
                .contains("readinessState")
                .contains("db")
                .contains("redis");
        // Storage group is prod-only (s3Storage bean exists only when STORAGE_PROVIDER=s3).
        assertThat(props.getProperty("management.endpoint.health.group.storage.include")).isNull();
        assertThat(props.getProperty("management.health.livenessstate.enabled")).isEqualTo("true");
        assertThat(props.getProperty("management.health.readinessstate.enabled")).isEqualTo("true");
    }

    @Test
    void applicationProdYml_definesStorageHealthGroup() {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(new ClassPathResource("application-prod.yml"));
        Properties props = factory.getObject();
        assertThat(props).isNotNull();
        assertThat(props.getProperty("management.endpoint.health.group.storage.include"))
                .isEqualTo("s3Storage");
    }
}
