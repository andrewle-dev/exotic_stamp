package metro.ExoticStamp.infra.storage.local;

import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.infra.storage.StorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    private static final String BASE_URL = "http://localhost:8080/uploads";

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.getLocal().setBasePath(tempDir.toString());
        props.getLocal().setBaseUrl(BASE_URL);
        storageService = new LocalStorageService(props);
    }

    @Test
    void uploadPng_succeeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ignored-name.png", "image/png", new byte[] {1, 2, 3});

        String url = storageService.upload(file, "public");

        assertTrue(url.startsWith(BASE_URL + "/public/"));
        assertTrue(url.endsWith(".png"));
        Path written = pathFromUrl(url);
        assertTrue(Files.isRegularFile(written));
        assertTrue(written.startsWith(tempDir.toAbsolutePath().normalize()));
        assertEquals(3, Files.size(written));
    }

    @Test
    void uploadJpeg_succeeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpeg", "image/jpeg", new byte[] {9, 8, 7});

        String url = storageService.upload(file, "public");

        assertTrue(url.endsWith(".jpg"));
        assertTrue(Files.isRegularFile(pathFromUrl(url)));
    }

    @Test
    void uploadWebp_usesWebpExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.webp", "image/webp", new byte[] {4, 5});

        String url = storageService.upload(file, "public");

        assertTrue(url.endsWith(".webp"));
        assertFalse(url.endsWith(".bin"));
        assertTrue(Files.isRegularFile(pathFromUrl(url)));
    }

    @Test
    void upload_createsMissingTargetDirectory() throws Exception {
        Path nested = tempDir.resolve("public");
        assertFalse(Files.exists(nested));

        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});
        storageService.upload(file, "public");

        assertTrue(Files.isDirectory(nested));
    }

    @Test
    void upload_returnsAbsoluteUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});

        String url = storageService.upload(file, "public");

        assertTrue(url.startsWith("http://localhost:8080/uploads/public/"));
        assertFalse(url.startsWith("/uploads"));
    }

    @Test
    void upload_writtenPathIsInsideBaseDirectory() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});

        String url = storageService.upload(file, "public");
        Path written = pathFromUrl(url);

        assertTrue(written.startsWith(storageService.getResolvedBasePath()));
        assertTrue(written.startsWith(tempDir.toAbsolutePath().normalize()));
    }

    @Test
    void upload_pathTraversalFolder_rejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});

        assertThrows(InvalidFileException.class, () -> storageService.upload(file, "../outside"));
        assertThrows(InvalidFileException.class, () -> storageService.upload(file, "public/../../outside"));
    }

    @Test
    void delete_existingFile_succeeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});
        String url = storageService.upload(file, "public");
        Path written = pathFromUrl(url);
        assertTrue(Files.exists(written));

        storageService.delete(url);

        assertFalse(Files.exists(written));
    }

    @Test
    void delete_missingFile_isIdempotent() {
        storageService.delete(BASE_URL + "/public/does-not-exist.png");
    }

    @Test
    void delete_unmanagedExternalUrl_isIgnored() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});
        String url = storageService.upload(file, "public");
        Path written = pathFromUrl(url);

        storageService.delete("https://cdn.example.com/other.png");

        assertTrue(Files.exists(written));
    }

    @Test
    void resolvedBasePath_isAbsolute() {
        assertTrue(storageService.getResolvedBasePath().isAbsolute());
        assertEquals(tempDir.toAbsolutePath().normalize(), storageService.getResolvedBasePath());
    }

    private Path pathFromUrl(String url) {
        String relative = url.substring(BASE_URL.length() + 1);
        return storageService.getResolvedBasePath().resolve(relative);
    }
}
