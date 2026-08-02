package metro.ExoticStamp.infra.storage.local;

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageService implements StorageService {

    private final Path basePath;
    private final ObjectKeyFactory objectKeyFactory;
    private final PublicUrlResolver publicUrlResolver;
    private final StorageMetrics storageMetrics;

    public LocalStorageService(
            StorageProperties storageProperties,
            ObjectKeyFactory objectKeyFactory,
            PublicUrlResolver publicUrlResolver,
            StorageMetrics storageMetrics
    ) {
        String configured = storageProperties.getLocal().getBasePath();
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("storage.local.base-path is required");
        }
        this.basePath = Path.of(configured).toAbsolutePath().normalize();
        this.objectKeyFactory = objectKeyFactory;
        this.publicUrlResolver = publicUrlResolver;
        this.storageMetrics = storageMetrics;
        initializeBaseDirectory();
        log.info("[LocalStorage] Resolved absolute base path: {}", this.basePath);
    }

    public Path getResolvedBasePath() {
        return basePath;
    }

    @Override
    public StorageUploadResult upload(StorageUploadRequest request) {
        if (request == null || request.file() == null || request.file().isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        String objectKey = objectKeyFactory.generate(request);
        objectKeyFactory.assertSafeObjectKey(objectKey);

        Path target = basePath.resolve(objectKey).normalize();
        assertUnderBase(target);
        Path targetDir = target.getParent();

        byte[] bytes;
        try {
            bytes = request.file().getBytes();
        } catch (IOException e) {
            storageMetrics.recordUploadFailure();
            throw new StorageWriteFailedException("Failed to read upload bytes", e);
        }

        if (Files.exists(target)) {
            objectKey = objectKeyFactory.generate(request);
            target = basePath.resolve(objectKey).normalize();
            assertUnderBase(target);
            targetDir = target.getParent();
            if (Files.exists(target)) {
                throw new StorageWriteFailedException("Unable to allocate unique object key");
            }
        }

        try {
            Files.createDirectories(targetDir);
            assertUnderBase(targetDir);
            Files.write(target, bytes);
        } catch (IOException e) {
            deleteQuietly(target);
            storageMetrics.recordUploadFailure();
            log.error(
                    "[LocalStorage] write failed basePath={} keyPrefix={} type={} cause={}",
                    basePath,
                    keyPrefix(objectKey),
                    e.getClass().getSimpleName(),
                    rootCauseMessage(e),
                    e);
            throw new StorageWriteFailedException("Failed to store file", e);
        }

        storageMetrics.recordUploadSuccess();
        String contentType = request.detectedContentType() != null
                ? request.detectedContentType()
                : request.file().getContentType();
        String publicUrl = request.visibility() == StorageVisibility.PUBLIC
                ? publicUrlResolver.toPublicUrl(objectKey)
                : null;
        return new StorageUploadResult(
                objectKey,
                publicUrl,
                contentType,
                bytes.length,
                sha256Hex(bytes),
                request.visibility(),
                "local"
        );
    }

    @Override
    public void delete(String fileUrlOrObjectKey) {
        if (fileUrlOrObjectKey == null || fileUrlOrObjectKey.isBlank()) {
            return;
        }
        try {
            Path path = resolvePathFromUrlOrKey(fileUrlOrObjectKey);
            if (path == null) {
                return;
            }
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            log.warn("[LocalStorage] delete skipped or failed err={}", e.getMessage());
        }
    }

    @Override
    public boolean exists(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return false;
        }
        try {
            objectKeyFactory.assertSafeObjectKey(objectKey);
            Path path = basePath.resolve(objectKey).normalize();
            if (!path.startsWith(basePath)) {
                return false;
            }
            return Files.exists(path);
        } catch (InvalidFileException ex) {
            return false;
        }
    }

    @Override
    public String createPresignedGetUrl(String objectKey) {
        objectKeyFactory.assertSafeObjectKey(objectKey);
        if (!objectKeyFactory.isPrivateKey(objectKey)) {
            throw new InvalidFileException("Presigned URLs are only issued for private objects");
        }
        // Local provider returns the derived URL (no signature) for private paths behind auth.
        return publicUrlResolver.toPublicUrl(objectKey);
    }

    private void initializeBaseDirectory() {
        try {
            Files.createDirectories(basePath);
            if (!Files.isDirectory(basePath)) {
                throw new IllegalStateException("storage.local.base-path is not a directory");
            }
            if (!Files.isWritable(basePath)) {
                throw new IllegalStateException("storage.local.base-path is not writable");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize local storage directory", e);
        }
    }

    private Path resolvePathFromUrlOrKey(String fileUrlOrObjectKey) {
        String objectKey = publicUrlResolver.tryExtractObjectKey(fileUrlOrObjectKey);
        if (objectKey == null) {
            if (fileUrlOrObjectKey.startsWith("public/")
                    || fileUrlOrObjectKey.startsWith("private/")
                    || fileUrlOrObjectKey.startsWith("temporary/")) {
                objectKey = fileUrlOrObjectKey;
            } else {
                log.warn("[LocalStorage] URL not under configured base, skip delete");
                return null;
            }
        }
        Path resolved = basePath.resolve(objectKey).normalize();
        if (!resolved.startsWith(basePath)) {
            log.warn("[LocalStorage] delete path traversal rejected");
            return null;
        }
        return resolved;
    }

    private void assertUnderBase(Path path) {
        if (!path.startsWith(basePath)) {
            throw new InvalidFileException("Invalid storage path");
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best-effort cleanup of partial writes
        }
    }

    private static String sha256Hex(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
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

    private static String rootCauseMessage(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
