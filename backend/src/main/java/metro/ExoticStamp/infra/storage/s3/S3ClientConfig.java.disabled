package metro.ExoticStamp.infra.storage.s3;

import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.infra.storage.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;
import java.time.Duration;

/**
 * Single managed S3Client + S3Presigner. Uses the AWS default credentials provider chain.
 * Static keys may only arrive via runtime environment / secret delivery — never from YAML.
 * Does not perform network calls during bean creation.
 */
@Configuration
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
@Slf4j
public class S3ClientConfig {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(StorageProperties storageProperties) {
        StorageProperties.S3 s3 = storageProperties.getS3();
        requireS3Config(s3);

        Duration connectTimeout = s3.getConnectTimeout() != null ? s3.getConnectTimeout() : Duration.ofSeconds(5);
        Duration apiCallTimeout = s3.getApiCallTimeout() != null ? s3.getApiCallTimeout() : Duration.ofSeconds(60);
        Duration attemptTimeout = s3.getApiCallAttemptTimeout() != null
                ? s3.getApiCallAttemptTimeout()
                : Duration.ofSeconds(20);
        int maxRetries = Math.max(0, s3.getMaxRetries());

        ClientOverrideConfiguration override = ClientOverrideConfiguration.builder()
                .apiCallTimeout(apiCallTimeout)
                .apiCallAttemptTimeout(attemptTimeout)
                .retryPolicy(RetryPolicy.builder(RetryMode.STANDARD)
                        .numRetries(maxRetries)
                        .build())
                .build();

        var builder = S3Client.builder()
                .region(Region.of(s3.getRegion().trim()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .httpClient(UrlConnectionHttpClient.builder()
                        .connectionTimeout(connectTimeout)
                        .socketTimeout(apiCallTimeout)
                        .build())
                .overrideConfiguration(override)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(s3.isPathStyleAccess())
                        .build());

        if (s3.getEndpoint() != null && !s3.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3.getEndpoint().trim()));
            log.info("[S3] endpoint override configured (dev/test LocalStack only)");
        }

        log.info("[S3] client configured region={} pathStyle={} (no network call at init)",
                s3.getRegion(), s3.isPathStyleAccess());
        return builder.build();
    }

    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(StorageProperties storageProperties) {
        StorageProperties.S3 s3 = storageProperties.getS3();
        requireS3Config(s3);
        var builder = S3Presigner.builder()
                .region(Region.of(s3.getRegion().trim()))
                .credentialsProvider(DefaultCredentialsProvider.create());
        if (s3.getEndpoint() != null && !s3.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3.getEndpoint().trim()));
        }
        return builder.build();
    }

    private static void requireS3Config(StorageProperties.S3 s3) {
        if (s3.getRegion() == null || s3.getRegion().isBlank()) {
            throw new IllegalStateException("storage.s3.region (AWS_REGION) is required when STORAGE_PROVIDER=s3");
        }
        if (s3.getBucket() == null || s3.getBucket().isBlank()) {
            throw new IllegalStateException("storage.s3.bucket (AWS_S3_BUCKET) is required when STORAGE_PROVIDER=s3");
        }
    }
}
