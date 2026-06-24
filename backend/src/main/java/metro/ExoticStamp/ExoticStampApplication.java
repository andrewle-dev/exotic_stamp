package metro.ExoticStamp;

import metro.ExoticStamp.config.CorsProperties;
import metro.ExoticStamp.config.RbacProperties;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.modules.auth.infrastructure.security.TokenRevocationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({RbacProperties.class, StorageProperties.class, CorsProperties.class,
        TokenRevocationProperties.class})
public class ExoticStampApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExoticStampApplication.class, args);
    }
}
