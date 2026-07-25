package metro.ExoticStamp.infra.storage.local;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.infra.storage.ObjectKeyFactory;
import metro.ExoticStamp.infra.storage.PublicUrlResolver;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageObjectCategory;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageUploadRequest;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceTest {

    private static final String BASE_URL = "http://localhost:8080/uploads";

    @TempDir
    Path tempDir;

    private LocalStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.setProvider("local");
        props.setPublicBaseUrl(BASE_URL);
        props.getLocal().setBasePath(tempDir.toString());
        props.getLocal().setBaseUrl(BASE_URL);
        storageService = new LocalStorageService(
                props,
                new ObjectKeyFactory(),
                new PublicUrlResolver(props),
                new StorageMetrics(new SimpleMeterRegistry()));
    }

    @Test
    void uploadPng_succeeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "ignored-name.png", "image/png", new byte[] {1, 2, 3});

        StorageUploadResult result = storageService.upload(request(file, StorageObjectCategory.TEMPORARY, null));

        assertTrue(result.publicUrl().startsWith(BASE_URL + "/public/temporary/"));
        assertTrue(result.objectKey().endsWith(".png"));
        Path written = pathFromUrl(result.publicUrl());
        assertTrue(Files.isRegularFile(written));
        assertEquals(3, Files.size(written));
    }

    @Test
    void uploadJpeg_succeeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpeg", "image/jpeg", new byte[] {9, 8, 7});

        StorageUploadResult result = storageService.upload(request(file, StorageObjectCategory.TEMPORARY, null));

        assertTrue(result.objectKey().endsWith(".jpg"));
        assertTrue(Files.isRegularFile(pathFromUrl(result.publicUrl())));
    }

    @Test
    void uploadWebp_usesWebpExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.webp", "image/webp", new byte[] {4, 5});

        StorageUploadResult result = storageService.upload(request(file, StorageObjectCategory.TEMPORARY, null));

        assertTrue(result.objectKey().endsWith(".webp"));
        assertFalse(result.objectKey().endsWith(".bin"));
    }

    @Test
    void uploadStationCover_usesVersionedKey() throws Exception {
        UUID stationId = UUID.fromString("00000000-0000-0000-0000-000000000501");
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});

        StorageUploadResult result = storageService.upload(
                request(file, StorageObjectCategory.STATION_COVER, stationId.toString()));

        assertTrue(result.objectKey().startsWith("public/stations/" + stationId + "/cover/"));
        assertTrue(Files.isRegularFile(pathFromUrl(result.publicUrl())));
    }

    @Test
    void upload_returnsAbsoluteUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});

        String url = storageService.upload(file, "ignored");

        assertTrue(url.startsWith("http://localhost:8080/uploads/public/temporary/"));
        assertFalse(url.startsWith("/uploads"));
    }

    @Test
    void delete_existingFile_succeeds() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});
        StorageUploadResult result = storageService.upload(request(file, StorageObjectCategory.TEMPORARY, null));
        Path written = pathFromUrl(result.publicUrl());
        assertTrue(Files.exists(written));

        storageService.delete(result.publicUrl());

        assertFalse(Files.exists(written));
    }

    @Test
    void delete_missingFile_isIdempotent() {
        storageService.delete(BASE_URL + "/public/temporary/does-not-exist.png");
    }

    @Test
    void delete_unmanagedExternalUrl_isIgnored() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.png", "image/png", new byte[] {1});
        StorageUploadResult result = storageService.upload(request(file, StorageObjectCategory.TEMPORARY, null));
        Path written = pathFromUrl(result.publicUrl());

        storageService.delete("https://cdn.example.com/other.png");

        assertTrue(Files.exists(written));
    }

    @Test
    void replace_doesNotDeleteOldObjectImmediately() throws Exception {
        MockMultipartFile first = new MockMultipartFile("file", "a.png", "image/png", new byte[] {1});
        MockMultipartFile second = new MockMultipartFile("file", "b.png", "image/png", new byte[] {2});
        UUID stationId = UUID.randomUUID();

        StorageUploadResult oldResult = storageService.upload(
                request(first, StorageObjectCategory.STATION_COVER, stationId.toString()));
        StorageUploadResult newResult = storageService.upload(
                request(second, StorageObjectCategory.STATION_COVER, stationId.toString()));

        assertTrue(Files.exists(pathFromUrl(oldResult.publicUrl())));
        assertTrue(Files.exists(pathFromUrl(newResult.publicUrl())));
        assertFalse(oldResult.objectKey().equals(newResult.objectKey()));
    }

    private static StorageUploadRequest request(MockMultipartFile file, StorageObjectCategory category, String entityId) {
        String contentType = file.getContentType();
        String ext = ObjectKeyFactory.extensionForContentType(contentType);
        return StorageUploadRequest.of(file, category, StorageVisibility.PUBLIC, entityId, contentType, ext);
    }

    private Path pathFromUrl(String url) {
        String relative = url.substring(BASE_URL.length() + 1);
        return storageService.getResolvedBasePath().resolve(relative);
    }
}
