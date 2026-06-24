package metro.ExoticStamp.infra.storage.local;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class LocalStorageService implements StorageService {

    private final StorageProperties storageProperties;

    @Override
    public String upload(MultipartFile file, String folder) {
        String normalizedFolder = normalizeFolder(folder);
        String ext = extensionForContentType(file.getContentType());
        String filename = UUID.randomUUID() + "." + ext;
        Path base = Paths.get(storageProperties.getLocal().getBasePath());
        Path targetDir = base.resolve(normalizedFolder);
        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);
            file.transferTo(target.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file", e);
        }
        String baseUrl = trimTrailingSlash(storageProperties.getLocal().getBaseUrl());
        return baseUrl + "/" + normalizedFolder + "/" + filename;
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }
        try {
            Path path = resolvePathFromUrl(fileUrl);
            if (path != null && Files.exists(path)) {
                Files.delete(path);
            }
        } catch (Exception e) {
            log.warn("[LocalStorageService] delete skipped or failed url={} err={}", fileUrl, e.getMessage());
        }
    }

    private Path resolvePathFromUrl(String fileUrl) {
        String baseUrl = trimTrailingSlash(storageProperties.getLocal().getBaseUrl());
        if (!fileUrl.startsWith(baseUrl)) {
            log.warn("[LocalStorageService] URL not under configured baseUrl, skip delete");
            return null;
        }
        String relative = fileUrl.substring(baseUrl.length());
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        return Paths.get(storageProperties.getLocal().getBasePath()).resolve(relative);
    }

    private static String normalizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "";
        }
        return folder.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
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
        return "bin";
    }
}
