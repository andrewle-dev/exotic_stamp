package metro.ExoticStamp.infra.storage;

import metro.ExoticStamp.common.exceptions.storage.FileTooLargeException;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageDimensionsException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileValidatorTest {

    private FileValidator validator;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.getFile().setMaxSizeMb(5);
        props.getFile().setAllowedTypes(List.of("image/jpeg", "image/png", "image/webp"));
        validator = new FileValidator(props);
    }

    @Test
    void validate_validJpeg_passes() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", new byte[10]);
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_invalidType_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.gif", "image/gif", new byte[10]);
        assertThrows(InvalidImageTypeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_unsupportedMime_rejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.svg", "image/svg+xml", new byte[10]);
        assertThrows(InvalidImageTypeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_exceedsSize_throws() {
        byte[] huge = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", huge);
        assertThrows(FileTooLargeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", new byte[0]);
        assertThrows(InvalidFileException.class, () -> validator.validate(file));
    }

    @Test
    void validate_webp_passes() {
        MockMultipartFile file = new MockMultipartFile("file", "a.webp", "image/webp", new byte[10]);
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_stampArtwork_tooSmall_rejected() throws Exception {
        MockMultipartFile file = png("stamp.png", 800, 800);
        InvalidImageDimensionsException ex = assertThrows(
                InvalidImageDimensionsException.class,
                () -> validator.validate(file, AssetUploadPurpose.STAMP_ARTWORK));
        assertTrue(ex.getMessage().contains("1024×1024"));
    }

    @Test
    void validate_stampArtwork_nonSquare_rejected() throws Exception {
        MockMultipartFile file = png("stamp.png", 1400, 1100);
        InvalidImageDimensionsException ex = assertThrows(
                InvalidImageDimensionsException.class,
                () -> validator.validate(file, AssetUploadPurpose.STAMP_ARTWORK));
        assertTrue(ex.getMessage().toLowerCase().contains("1:1"));
    }

    @Test
    void validate_stampArtwork_validSquare_passes() throws Exception {
        MockMultipartFile file = png("stamp.png", 1024, 1024);
        assertDoesNotThrow(() -> validator.validate(file, AssetUploadPurpose.STAMP_ARTWORK));
    }

    @Test
    void validate_partnerLogo_tooSmall_rejected() throws Exception {
        MockMultipartFile file = png("logo.png", 400, 400);
        InvalidImageDimensionsException ex = assertThrows(
                InvalidImageDimensionsException.class,
                () -> validator.validate(file, AssetUploadPurpose.PARTNER_LOGO));
        assertTrue(ex.getMessage().contains("512×512"));
    }

    @Test
    void validate_partnerLogo_farFromSquare_rejected() throws Exception {
        MockMultipartFile file = png("logo.png", 900, 600);
        InvalidImageDimensionsException ex = assertThrows(
                InvalidImageDimensionsException.class,
                () -> validator.validate(file, AssetUploadPurpose.PARTNER_LOGO));
        assertTrue(ex.getMessage().toLowerCase().contains("1:1"));
    }

    @Test
    void validate_partnerBanner_tooSmall_rejected() throws Exception {
        MockMultipartFile file = png("banner.png", 640, 360);
        InvalidImageDimensionsException ex = assertThrows(
                InvalidImageDimensionsException.class,
                () -> validator.validate(file, AssetUploadPurpose.PARTNER_BANNER));
        assertTrue(ex.getMessage().contains("1280×720"));
    }

    @Test
    void validate_partnerBanner_badRatio_rejected() throws Exception {
        MockMultipartFile file = png("banner.png", 1280, 1280);
        InvalidImageDimensionsException ex = assertThrows(
                InvalidImageDimensionsException.class,
                () -> validator.validate(file, AssetUploadPurpose.PARTNER_BANNER));
        assertTrue(ex.getMessage().contains("16:9"));
    }

    @Test
    void validate_partnerBanner_valid_passes() throws Exception {
        MockMultipartFile file = png("banner.png", 1920, 1080);
        assertDoesNotThrow(() -> validator.validate(file, AssetUploadPurpose.PARTNER_BANNER));
    }

    private static MockMultipartFile png(String name, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return new MockMultipartFile("file", name, "image/png", out.toByteArray());
    }
}
