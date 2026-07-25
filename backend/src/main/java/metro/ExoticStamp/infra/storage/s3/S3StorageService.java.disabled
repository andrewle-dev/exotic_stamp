package metro.ExoticStamp.infra.storage.s3;

import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.StorageWriteFailedException;
import metro.ExoticStamp.infra.storage.ObjectKeyFactory;
import metro.ExoticStamp.infra.storage.PublicUrlResolver;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.infra.storage.StorageUploadRequest;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
@Slf4j
public class S3StorageService implements StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final StorageProperties storageProperties;
    private final ObjectKeyFactory objectKeyFactory;
    private final PublicUrlResolver publicUrlResolver;
    private final StorageMetrics storageMetrics;

    public S3StorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            StorageProperties storageProperties,
            ObjectKeyFactory objectKeyFactory,
            PublicUrlResolver publicUrlResolver,
            StorageMetrics storageMetrics
    ) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.storageProperties = storageProperties;
        this.objectKeyFactory = objectKeyFactory;
        this.publicUrlResolver = publicUrlResolver;
        this.storageMetrics = storageMetrics;
    }

    @Override
    public StorageUploadResult upload(StorageUploadRequest request) {
        if (request == null || request.file() == null || request.file().isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        String objectKey = objectKeyFactory.generate(request);
        objectKeyFactory.assertSafeObjectKey(objectKey);
        if (exists(objectKey)) {
            objectKey = objectKeyFactory.generate(request);
            if (exists(objectKey)) {
                throw new StorageWriteFailedException("Unable to allocate unique object key");
            }
        }

        String contentType = request.detectedContentType() != null
                ? request.detectedContentType()
                : request.file().getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidFileException("Content type is required");
        }

        byte[] bytes;
        try {
            bytes = request.file().getBytes();
        } catch (IOException e) {
            storageMetrics.recordUploadFailure();
            throw new StorageWriteFailedException("Failed to read upload bytes", e);
        }

        String checksum = sha256Hex(bytes);
        String cacheControl = ObjectKeyFactory.cacheControlFor(request.visibility());
        String bucket = storageProperties.getS3().getBucket();

        try {
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .cacheControl(cacheControl)
                    .metadata(Map.of("checksum-sha256", checksum))
                    .build();
            s3Client.putObject(put, RequestBody.fromBytes(bytes));
        } catch (Exception e) {
            storageMetrics.recordUploadFailure();
            log.error("[S3] PutObject failed keyPrefix={} type={}",
                    keyPrefix(objectKey), e.getClass().getSimpleName());
            throw S3ExceptionMapper.mapUploadFailure(e);
        }

        storageMetrics.recordUploadSuccess();
        String publicUrl = request.visibility() == StorageVisibility.PUBLIC
                ? publicUrlResolver.toPublicUrl(objectKey)
                : null;
        return new StorageUploadResult(
                objectKey,
                publicUrl,
                contentType,
                bytes.length,
                checksum,
                request.visibility(),
                "s3"
        );
    }

    @Override
    public void delete(String fileUrlOrObjectKey) {
        if (fileUrlOrObjectKey == null || fileUrlOrObjectKey.isBlank()) {
            return;
        }
        String objectKey = publicUrlResolver.tryExtractObjectKey(fileUrlOrObjectKey);
        if (objectKey == null) {
            objectKey = fileUrlOrObjectKey;
        }
        try {
            objectKeyFactory.assertSafeObjectKey(objectKey);
        } catch (InvalidFileException ex) {
            log.warn("[S3] delete skipped — invalid key");
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket())
                    .key(objectKey)
                    .build());
        } catch (Exception e) {
            if (!S3ExceptionMapper.isNotFound(e)) {
                log.warn("[S3] delete failed keyPrefix={} type={}",
                        keyPrefix(objectKey), e.getClass().getSimpleName());
            }
        }
    }

    @Override
    public boolean exists(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return false;
        }
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(storageProperties.getS3().getBucket())
                    .key(objectKey)
                    .build());
            return true;
        } catch (Exception e) {
            if (S3ExceptionMapper.isNotFound(e)) {
                return false;
            }
            log.warn("[S3] HeadObject failed keyPrefix={} type={}",
                    keyPrefix(objectKey), e.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public String createPresignedGetUrl(String objectKey) {
        objectKeyFactory.assertSafeObjectKey(objectKey);
        if (!objectKeyFactory.isPrivateKey(objectKey)) {
            throw new InvalidFileException("Presigned URLs are only issued for private objects");
        }
        Duration expiry = storageProperties.getS3().getPresignExpiry() != null
                ? storageProperties.getS3().getPresignExpiry()
                : Duration.ofMinutes(15);
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(storageProperties.getS3().getBucket())
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiry)
                .getObjectRequest(getObjectRequest)
                .build();
        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    /** Bounded low-cost health probe — HeadBucket only. */
    public boolean probeBucket() {
        try {
            s3Client.headBucket(b -> b.bucket(storageProperties.getS3().getBucket()));
            return true;
        } catch (Exception e) {
            log.warn("[S3] health probe failed type={}", e.getClass().getSimpleName());
            return false;
        }
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String keyPrefix(String objectKey) {
        if (objectKey == null) {
            return "null";
        }
        int idx = objectKey.lastIndexOf('/');
        return idx > 0 ? objectKey.substring(0, idx) : objectKey;
    }
}
