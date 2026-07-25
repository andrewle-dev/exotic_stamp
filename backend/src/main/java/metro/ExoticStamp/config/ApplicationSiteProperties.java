package metro.ExoticStamp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Public site URLs. Production binds {@code current} from {@code FRONTEND_URL} / {@code BACKEND_URL}.
 */
@ConfigurationProperties(prefix = "application")
@Validated
@Getter
@Setter
public class ApplicationSiteProperties {

    private SiteUrls frontend = new SiteUrls();
    private SiteUrls backend = new SiteUrls();

    @Getter
    @Setter
    public static class SiteUrls {
        /** Active URL for the current profile (required in prod). */
        private String current;
        private String dev;
        private String prod;
    }
}
