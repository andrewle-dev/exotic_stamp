package metro.ExoticStamp.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "application.cors")
@Validated
@Getter
@Setter
public class CorsProperties {

    private String allowedOrigins = "http://localhost:3000,http://localhost:5173";
    private String allowedMethods = "GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD";
    private String allowedHeaders = "*";
    private boolean allowCredentials = true;

    @PostConstruct
    void rejectWildcardOriginsWithCredentials() {
        if (!allowCredentials) {
            return;
        }
        for (String origin : splitCsv(allowedOrigins)) {
            if ("*".equals(origin)) {
                throw new IllegalStateException(
                        "CORS allowCredentials=true cannot be used with allowedOrigins='*'");
            }
        }
    }

    public List<String> allowedOriginsList() {
        return splitCsv(allowedOrigins);
    }

    public List<String> allowedMethodsList() {
        return splitCsv(allowedMethods);
    }

    public List<String> allowedHeadersList() {
        return splitCsv(allowedHeaders);
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
