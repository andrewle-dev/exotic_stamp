package metro.ExoticStamp.modules.community.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "community")
public class CommunityProperties {

    private int defaultPageSize = 20;

    private int maxPageSize = 50;

    private int maxMetadataBytes = 2048;

    private int referralCodeMaxAttempts = 5;
}
