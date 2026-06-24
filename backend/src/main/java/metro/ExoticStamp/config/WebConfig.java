package metro.ExoticStamp.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final DeprecatedLegacyCollectionsInterceptor deprecatedLegacyCollectionsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(deprecatedLegacyCollectionsInterceptor)
                .addPathPatterns("/api/v1/collections/**");
    }
}
