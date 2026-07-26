package metro.ExoticStamp.infra.storage.s3;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.StorageWriteFailedException;
import metro.ExoticStamp.infra.storage.ObjectKeyFactory;
import metro.ExoticStamp.infra.storage.PublicUrlResolver;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageObjectCategory;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageUploadRequest;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers(disabledWithoutDocker = true)
class S3StorageLocalStackIT {

    private static final String BUCKET = "exotic-stamp-test";
    private static final String PUBLIC_BASE = "https://cdn.test.example/media";

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.8"))
            .withServices(S3)
            .withEnv("AWS_ACCESS_KEY_ID", "test")
            .withEnv("AWS_SECRET_ACCESS_KEY", "test");

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private StorageProperties storageProperties;
    private S3StorageService storageService;

    @BeforeAll
    static void createBucket() {
        try (S3Client bootstrap = buildClient()) {
            bootstrap.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
        }
    }

    @BeforeEach
    void setUp() {
        System.setProperty("aws.accessKeyId", "test");
        System.setProperty("aws.secretAccessKey", "test");

        storageProperties = new StorageProperties();
        storageProperties.setProvider("s3");
        storageProperties.setPublicBaseUrl(PUBLIC_BASE);
        storageProperties.getS3().setBucket(BUCKET);
        storageProperties.getS3().setRegion(localstack.getRegion());
        storageProperties.getS3().setEndpoint(localstack.getEndpointOverride(S3).toString());
        storageProperties.getS3().setPathStyleAccess(true);

        s3Client = buildClient();
        s3Presigner = S3Presigner.builder()
                .region(Region.of(localstack.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .endpointOverride(localstack.getEndpointOverride(S3))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();

        storageService = new S3StorageService(
                s3Client,
                s3Presigner,
                storageProperties,
                new ObjectKeyFactory(),
                new PublicUrlResolver(storageProperties),
                new StorageMetrics(new SimpleMeterRegistry()));
    }

    @Test
    void upload_jpegPngWebp_thenHeadExists() throws Exception {
        UUID stationId = UUID.randomUUID();

        StorageUploadResult jpeg = storageService.upload(request(
                jpegBytes(64, 64), "image/jpeg", "jpg", stationId));
        StorageUploadResult png = storageService.upload(request(
                pngBytes(64, 64), "image/png", "png", stationId));
        StorageUploadResult webp = storageService.upload(request(
                minimalWebpVp8x(64, 64), "image/webp", "webp", stationId));

        assertThat(storageService.exists(jpeg.objectKey())).isTrue();
        assertThat(storageService.exists(png.objectKey())).isTrue();
        assertThat(storageService.exists(webp.objectKey())).isTrue();

        s3Client.headObject(HeadObjectRequest.builder().bucket(BUCKET).key(jpeg.objectKey()).build());
        assertThat(jpeg.publicUrl()).startsWith(PUBLIC_BASE + "/");
        assertThat(png.publicUrl()).contains("/public/stations/");
        assertThat(webp.objectKey()).endsWith(".webp");
    }

    @Test
    void createPresignedGetUrl_rejectsPublicKey() throws Exception {
        StorageUploadResult uploaded = storageService.upload(request(
                pngBytes(32, 32), "image/png", "png", UUID.randomUUID()));
        assertThat(uploaded.objectKey()).startsWith("public/");

        assertThatThrownBy(() -> storageService.createPresignedGetUrl(uploaded.objectKey()))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("private");
    }

    @Test
    void upload_wrongBucket_fails() throws Exception {
        storageProperties.getS3().setBucket("does-not-exist-bucket");
        S3StorageService badBucketService = new S3StorageService(
                s3Client,
                s3Presigner,
                storageProperties,
                new ObjectKeyFactory(),
                new PublicUrlResolver(storageProperties),
                new StorageMetrics(new SimpleMeterRegistry()));

        assertThatThrownBy(() -> badBucketService.upload(request(
                pngBytes(32, 32), "image/png", "png", UUID.randomUUID())))
                .isInstanceOf(StorageWriteFailedException.class);
    }

    private static StorageUploadRequest request(
            byte[] bytes, String contentType, String ext, UUID stationId) {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cover." + ext, contentType, bytes);
        return StorageUploadRequest.of(
                file,
                StorageObjectCategory.STATION_COVER,
                StorageVisibility.PUBLIC,
                stationId,
                contentType,
                ext);
    }

    private static S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(localstack.getEndpointOverride(S3))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .region(Region.of(localstack.getRegion()))
                .httpClient(UrlConnectionHttpClient.create())
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private static byte[] pngBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static byte[] jpegBytes(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", out);
        return out.toByteArray();
    }

    private static byte[] minimalWebpVp8x(int width, int height) {
        ByteBuffer buf = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN);
        buf.put("RIFF".getBytes());
        buf.putInt(22);
        buf.put("WEBP".getBytes());
        buf.put("VP8X".getBytes());
        buf.putInt(10);
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 0);
        int w = width - 1;
        int h = height - 1;
        buf.put((byte) (w & 0xFF));
        buf.put((byte) ((w >> 8) & 0xFF));
        buf.put((byte) ((w >> 16) & 0xFF));
        buf.put((byte) (h & 0xFF));
        buf.put((byte) ((h >> 8) & 0xFF));
        buf.put((byte) ((h >> 16) & 0xFF));
        return buf.array();
    }
}
