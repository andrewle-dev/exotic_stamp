package metro.ExoticStamp.infra.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    @NotBlank
    private String provider = "local";

    private Local local = new Local();

    private FileConstraints file = new FileConstraints();

    private S3 s3 = new S3();

    @Data
    public static class Local {
        private String basePath;
        private String baseUrl;
    }

    @Data
    public static class FileConstraints {
        private long maxSizeMb = 5;
        private List<String> allowedTypes = List.of("image/jpeg", "image/png");
    }

    @Data
    public static class S3 {
        private String bucket;
        private String region;
        private String endpoint;
    }
}
