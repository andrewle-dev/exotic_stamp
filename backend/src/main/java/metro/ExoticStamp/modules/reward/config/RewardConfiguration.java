package metro.ExoticStamp.modules.reward.config;

import metro.ExoticStamp.modules.reward.domain.service.MilestoneDomainService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RewardProperties.class)
public class RewardConfiguration {

    @Bean
    public MilestoneDomainService milestoneDomainService() {
        return new MilestoneDomainService();
    }
}
