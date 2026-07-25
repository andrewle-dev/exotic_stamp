package metro.ExoticStamp.infra.storage;

import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

class ObjectKeyFactoryTest {

    private final ObjectKeyFactory factory = new ObjectKeyFactory(
            Clock.fixed(Instant.parse("2026-07-24T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void stationCover_usesDetectedExtensionNotFilename() {
        String key = factory.generate(req(
                StorageObjectCategory.STATION_COVER,
                UUID.randomUUID().toString(),
                IMAGE_JPEG_VALUE,
                "jpg"));
        assertThat(key).matches("public/stations/[0-9a-f-]+/cover/[0-9a-f-]+\\.jpg");
        assertThat(key).doesNotContain("evil");
    }

    @Test
    void temporary_usesDatePrefixUnderPublic() {
        String key = factory.generate(req(StorageObjectCategory.TEMPORARY, null, IMAGE_PNG_VALUE, "png"));
        assertThat(key).startsWith("public/temporary/2026/07/24/");
        assertThat(key).endsWith(".png");
    }

    @Test
    void privateUser_keyIsPrivate() {
        String key = factory.generate(req(
                StorageObjectCategory.USER_PRIVATE,
                UUID.randomUUID().toString(),
                IMAGE_PNG_VALUE,
                "png"));
        assertThat(key).startsWith("private/users/");
        assertThat(factory.isPrivateKey(key)).isTrue();
        assertThat(factory.isPublicKey(key)).isFalse();
    }

    @Test
    void pathTraversalEntityId_rejected() {
        assertThatThrownBy(() -> factory.generate(req(
                StorageObjectCategory.STATION_COVER,
                "../etc",
                IMAGE_PNG_VALUE,
                "png")))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void assertSafeObjectKey_rejectsTraversal() {
        assertThatThrownBy(() -> factory.assertSafeObjectKey("public/../secret"))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    void cacheControl_publicIsImmutable() {
        assertThat(ObjectKeyFactory.cacheControlFor(StorageVisibility.PUBLIC))
                .contains("immutable");
        assertThat(ObjectKeyFactory.cacheControlFor(StorageVisibility.PRIVATE))
                .contains("no-store");
    }

    @Test
    void contentTypeForExtension() {
        assertThat(ObjectKeyFactory.contentTypeForExtension("jpg")).isEqualTo(IMAGE_JPEG_VALUE);
        assertThat(ObjectKeyFactory.contentTypeForExtension("png")).isEqualTo(IMAGE_PNG_VALUE);
        assertThat(ObjectKeyFactory.contentTypeForExtension("webp")).isEqualTo("image/webp");
    }

    private static StorageUploadRequest req(
            StorageObjectCategory category, String entityId, String contentType, String ext) {
        return StorageUploadRequest.of(null, category, StorageVisibility.PUBLIC, entityId, contentType, ext);
    }
}
