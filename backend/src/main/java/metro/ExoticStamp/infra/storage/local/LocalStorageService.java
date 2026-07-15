package metro.ExoticStamp.infra.storage.local;

import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.StorageWriteFailedException;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@Slf4j
public class LocalStorageService implements StorageService {

    private final Path basePath;
    private final String baseUrl;

    public LocalStorageService(StorageProperties storageProperties) {
        String configured = storageProperties.getLocal().getBasePath();
        if (configured == null || configured.isBlank()) {
            throw new IllegalStateException("storage.local.base-path is required");
        }
        this.basePath = Path.of(configured).toAbsolutePath().normalize();
        this.baseUrl = trimTrailingSlash(storageProperties.getLocal().getBaseUrl());
        initializeBaseDirectory();
        log.info("[LocalStorage] Resolved absolute base path: {}", this.basePath);
    }

    /**
     * Absolute, normalized storage root used by upload and static serving.
     */
    public Path getResolvedBasePath() {
        return basePath;
    }

    @Override
    public String upload(MultipartFile file, String folder) {
        String normalizedFolder = normalizeFolder(folder);
        String ext = extensionForContentType(file.getContentType());
        String filename = UUID.randomUUID() + "." + ext;

        Path targetDir = resolveUnderBase(normalizedFolder);
        Path target = targetDir.resolve(filename).normalize();
        assertUnderBase(target);

        try {
            Files.createDirectories(targetDir);
            assertUnderBase(targetDir);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            deleteQuietly(target);
            log.error(
                    "[LocalStorage] write failed basePath={} folder={} filename={} type={} cause={}",
                    basePath,
                    normalizedFolder,
                    filename,
                    e.getClass().getSimpleName(),
                    rootCauseMessage(e),
                    e);
            throw new StorageWriteFailedException("Failed to store file", e);
        }

        if (normalizedFolder.isEmpty()) {
            return baseUrl + "/" + filename;
        }
        return baseUrl + "/" + normalizedFolder + "/" + filename;
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            Path path = resolvePathFromUrl(fileUrl);
            if (path == null) {
                return;
            }
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            log.warn("[LocalStorage] delete skipped or failed url={} err={}", fileUrl, e.getMessage());
        }
    }

    private void initializeBaseDirectory() {
        try {
            Files.createDirectories(basePath);
            if (!Files.isDirectory(basePath)) {
                log.error("[LocalStorage] base path is not a directory: {}", basePath);
                throw new IllegalStateException("storage.local.base-path is not a directory");
            }
            if (!Files.isWritable(basePath)) {
                log.error("[LocalStorage] base path is not writable: {}", basePath);
                throw new IllegalStateException("storage.local.base-path is not writable");
            }
        } catch (IOException e) {
            log.error(
                    "[LocalStorage] failed to initialize base path={} type={} cause={}",
                    basePath,
                    e.getClass().getSimpleName(),
                    rootCauseMessage(e),
                    e);
            throw new IllegalStateException("Failed to initialize local storage directory", e);
        }
    }

    private Path resolvePathFromUrl(String fileUrl) {
        if (!fileUrl.startsWith(baseUrl)) {
            log.warn("[LocalStorage] URL not under configured baseUrl, skip delete");
            return null;
        }
        String relative = fileUrl.substring(baseUrl.length());
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        if (relative.isBlank()) {
            return null;
        }
        Path resolved = basePath.resolve(relative).normalize();
        if (!resolved.startsWith(basePath)) {
            log.warn("[LocalStorage] delete path traversal rejected");
            return null;
        }
        return resolved;
    }

    private Path resolveUnderBase(String normalizedFolder) {
        Path targetDir = normalizedFolder.isEmpty()
                ? basePath
                : basePath.resolve(normalizedFolder).normalize();
        assertUnderBase(targetDir);
        return targetDir;
    }

    private void assertUnderBase(Path path) {
        if (!path.startsWith(basePath)) {
            throw new InvalidFileException("Invalid storage folder");
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best-effort cleanup of partial writes
        }
    }

    private static String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "";
        }
        String normalized = folder.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
        if (normalized.contains("..")) {
            throw new InvalidFileException("Invalid storage folder");
        }
        return normalized;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.replaceAll("/+$", "");
    }

    private static String extensionForContentType(String contentType) {
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }
        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }
        return "bin";
    }

    private static String rootCauseMessage(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage();
    }
}
