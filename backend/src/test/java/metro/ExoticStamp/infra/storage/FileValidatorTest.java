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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
        props.getFile().setMaxWidth(2560);
        props.getFile().setMaxHeight(2560);
        props.getFile().setMaxPixels(2560L * 2560L);
        props.getFile().setAllowedTypes(List.of("image/jpeg", "image/png", "image/webp"));
        validator = new FileValidator(props);
    }

    @Test
    void validate_validJpeg_passes() throws Exception {
        MockMultipartFile file = jpeg("a.jpg", 64, 64);
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_validPng_passes() throws Exception {
        MockMultipartFile file = png("a.png", 64, 64);
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_validWebpVp8x_passes() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.webp", "image/webp", minimalWebpVp8x(100, 80));
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_contentTypeMismatch_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/png", jpegBytes(32, 32));
        assertThrows(InvalidImageTypeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_imageJpgAlias_normalized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpg", jpegBytes(32, 32));
        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void validate_svgAsPngMime_rejectedByMagic() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.svg", "image/png", "<svg xmlns='http://www.w3.org/2000/svg'></svg>".getBytes());
        assertThrows(InvalidImageTypeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_pathTraversalFilename_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../etc/passwd.png", "image/png", pngBytes(32, 32));
        assertThrows(InvalidFileException.class, () -> validator.validate(file));
    }

    @Test
    void validate_doubleExtension_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg.exe", "image/png", pngBytes(32, 32));
        assertThrows(InvalidFileException.class, () -> validator.validate(file));
    }

    @Test
    void validate_unsupportedMime_rejected() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.gif", "image/gif", jpegBytes(32, 32));
        assertThrows(InvalidImageTypeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_exceedsSize_throws() {
        byte[] huge = new byte[6 * 1024 * 1024];
        // JPEG magic so we fail on size before magic mismatch
        huge[0] = (byte) 0xFF;
        huge[1] = (byte) 0xD8;
        huge[2] = (byte) 0xFF;
        MockMultipartFile file = new MockMultipartFile("file", "a.jpg", "image/jpeg", huge);
        assertThrows(FileTooLargeException.class, () -> validator.validate(file));
    }

    @Test
    void validate_emptyFile_throws() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", "image/jpeg", new byte[0]);
        assertThrows(InvalidFileException.class, () -> validator.validate(file));
    }

    @Test
    void validate_exceedsMaxPixels_rejected() throws Exception {
        StorageProperties props = new StorageProperties();
        props.getFile().setMaxSizeMb(5);
        props.getFile().setMaxWidth(10000);
        props.getFile().setMaxHeight(10000);
        props.getFile().setMaxPixels(100);
        props.getFile().setAllowedTypes(List.of("image/png"));
        FileValidator tight = new FileValidator(props);

        MockMultipartFile file = png("big.png", 20, 20);
        assertThrows(InvalidImageDimensionsException.class, () -> tight.validate(file));
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
        return new MockMultipartFile("file", name, "image/png", pngBytes(width, height));
    }

    private static MockMultipartFile jpeg(String name, int width, int height) throws IOException {
        return new MockMultipartFile("file", name, "image/jpeg", jpegBytes(width, height));
    }

    private static byte[] pngBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    private static byte[] jpegBytes(int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpeg", out);
        return out.toByteArray();
    }

    /** Minimal RIFF/WEBP with VP8X canvas size fields. */
    private static byte[] minimalWebpVp8x(int width, int height) {
        ByteBuffer buf = ByteBuffer.allocate(30).order(ByteOrder.LITTLE_ENDIAN);
        buf.put("RIFF".getBytes());
        buf.putInt(22); // file size minus 8
        buf.put("WEBP".getBytes());
        buf.put("VP8X".getBytes());
        buf.putInt(10); // chunk size
        buf.put((byte) 0); // flags
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.put((byte) 0);
        int w = width - 1;
        int h = height - 1;
        buf.put((byte) (w & 0xFF));
        buf.put((byte) ((w >> 8) & 0xFF));
        buf.put((byte) ((w >> 16) & 0xFF));
        buf.put((byte) (h & 0xFF));
        buf.put((byte) ((h >> 8) & 0xFF));
        buf.put((byte) ((h >> 16) & 0xFF));
        return buf.array();
    }
}
