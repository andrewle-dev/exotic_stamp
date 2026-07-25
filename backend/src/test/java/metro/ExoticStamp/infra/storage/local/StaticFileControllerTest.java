package metro.ExoticStamp.infra.storage.local;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.infra.storage.ObjectKeyFactory;
import metro.ExoticStamp.infra.storage.PublicUrlResolver;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticFileControllerTest {

    private static final String BASE_URL = "http://localhost:8080/uploads";

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;
    private StaticFileController controller;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.setProvider("local");
        props.getLocal().setBasePath(tempDir.toString());
        props.getLocal().setBaseUrl(BASE_URL);
        props.setPublicBaseUrl(BASE_URL);
        storageService = new LocalStorageService(
                props,
                new ObjectKeyFactory(),
                new PublicUrlResolver(props),
                new StorageMetrics(new SimpleMeterRegistry()));
        controller = new StaticFileController(storageService);
    }

    @Test
    void getPublicUpload_returnsUploadedImageWithContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {10, 20, 30});
        String url = storageService.upload(file, "public");
        String relative = url.substring(BASE_URL.length() + 1);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/uploads/" + relative);
        request.setRequestURI("/uploads/" + relative);

        var response = controller.serve(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(MediaType.IMAGE_PNG_VALUE, response.getHeaders().getFirst("Content-Type"));
        assertNotNull(response.getBody());
        assertTrue(response.getBody().exists());
    }

    @Test
    void getPublicUpload_missingFile_returns404() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/uploads/public/missing.png");
        request.setRequestURI("/uploads/public/missing.png");

        var response = controller.serve(request);

        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }

    @Test
    void getPublicUpload_pathTraversal_returns404() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/uploads/public/../../secret.txt");
        request.setRequestURI("/uploads/public/../../secret.txt");

        var response = controller.serve(request);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getUploadsRoot_returns404() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/uploads/");
        request.setRequestURI("/uploads/");

        var response = controller.serve(request);

        assertEquals(404, response.getStatusCode().value());
    }
}
