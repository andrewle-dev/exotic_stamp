package metro.ExoticStamp.infra.storage.local;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class StaticFileController {

    private static final String UPLOADS_PREFIX = "/uploads";

    private final LocalStorageService localStorageService;

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
        if (relative.isBlank() || relative.endsWith("/")) {
            return ResponseEntity.notFound().build();
        }
        Path base = localStorageService.getResolvedBasePath();
        Path file = base.resolve(relative).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file.toFile());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, resolveContentType(file))
                .body(resource);
    }

    private static String resolveContentType(Path file) throws IOException {
        String probed = Files.probeContentType(file);
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".png")) {
            return MediaType.IMAGE_PNG_VALUE;
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG_VALUE;
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        if (probed != null && !probed.isBlank()) {
            return probed;
        }
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
