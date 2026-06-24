package metro.ExoticStamp.infra.storage.local;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.infra.storage.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class StaticFileController {

    private static final String UPLOADS_PREFIX = "/uploads";

    private final StorageProperties storageProperties;

    @GetMapping("/uploads/**")
    public ResponseEntity<Resource> serve(HttpServletRequest request) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        String prefix = contextPath + UPLOADS_PREFIX;
        if (!uri.startsWith(prefix)) {
            return ResponseEntity.notFound().build();
        }
        String relative = uri.substring(prefix.length());
        if (relative.startsWith("/")) {
            relative = relative.substring(1);
        }
        if (relative.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        Path base = Paths.get(storageProperties.getLocal().getBasePath()).normalize();
        Path file = base.resolve(relative).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file.toFile());
        String contentType = Files.probeContentType(file);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
}
