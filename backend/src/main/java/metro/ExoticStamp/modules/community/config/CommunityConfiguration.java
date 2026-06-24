package metro.ExoticStamp.modules.community.config;

import metro.ExoticStamp.modules.community.domain.service.ReferralCodeGenerator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(CommunityProperties.class)
public class CommunityConfiguration {

    @Bean
    public ReferralCodeGenerator referralCodeGenerator() {
        return new ReferralCodeGenerator();
    }
}
