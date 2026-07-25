package metro.ExoticStamp.infra.storage;

import metro.ExoticStamp.common.exceptions.storage.FileTooLargeException;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageDimensionsException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Component
public class FileValidator {

    /** Tight tolerance for strict 1:1 artwork (≈2%). */
    private static final double STRICT_SQUARE_TOLERANCE = 0.02;
    /** Soft tolerance for logos / thumbs near 1:1 (≈5%). */
    private static final double NEAR_SQUARE_TOLERANCE = 0.05;
    /** Soft tolerance for landscape banners near 16:9 (≈5%). */
    private static final double LANDSCAPE_16_9_TOLERANCE = 0.05;
    private static final double TARGET_16_9 = 16.0 / 9.0;

    private static final byte[] PNG_SIGNATURE = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };
    private static final Set<String> DANGEROUS_TRAILING_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "msi", "scr", "js", "jar", "sh", "ps1", "php", "html", "htm", "svg"
    );

    private final StorageProperties storageProperties;

    public FileValidator(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public void validate(MultipartFile file) {
        validateAndDetect(file, AssetUploadPurpose.GENERIC);
    }

    public void validate(MultipartFile file, AssetUploadPurpose purpose) {
        validateAndDetect(file, purpose);
    }

    /**
     * Validates upload content and returns detected content-type / extension for object keys.
     * Must complete before any storage PutObject / local write.
     */
    public DetectedUpload validateAndDetect(MultipartFile file, AssetUploadPurpose purpose) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }

        StorageProperties.FileConstraints constraints = storageProperties.getFile();
        long maxBytes = constraints.getMaxSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(
                    "File exceeds maximum size of " + constraints.getMaxSizeMb() + " MB");
        }

        sanitizeOriginalFilename(file.getOriginalFilename());

        byte[] bytes = readAllBytes(file);
        if (bytes.length == 0) {
            throw new InvalidFileException("File is required");
        }
        if (bytes.length > maxBytes) {
            throw new FileTooLargeException(
                    "File exceeds maximum size of " + constraints.getMaxSizeMb() + " MB");
        }

        DetectedImage detected = detectByMagic(bytes);
        String declared = normalizeContentType(file.getContentType());
        if (declared == null) {
            throw new InvalidImageTypeException("Content type is missing");
        }
        if (!declared.equals(detected.contentType())) {
            throw new InvalidImageTypeException(
                    "Content type does not match file contents: declared=" + declared
                            + ", detected=" + detected.contentType());
        }
        if (!constraints.getAllowedTypes().contains(detected.contentType())) {
            throw new InvalidImageTypeException("Unsupported image type: " + detected.contentType());
        }

        ImageSize size = switch (detected.format()) {
            case JPEG -> readJpegDimensions(bytes);
            case PNG -> readPngDimensions(bytes);
            case WEBP -> readWebpDimensions(bytes);
        };

        if (size.width() <= 0 || size.height() <= 0) {
            throw new InvalidImageDimensionsException("Unable to read image dimensions.");
        }
        if (size.width() > constraints.getMaxWidth() || size.height() > constraints.getMaxHeight()) {
            throw new InvalidImageDimensionsException(
                    "Image exceeds maximum dimensions of "
                            + constraints.getMaxWidth() + "×" + constraints.getMaxHeight() + ".");
        }
        long pixels = (long) size.width() * (long) size.height();
        if (pixels > constraints.getMaxPixels()) {
            throw new InvalidImageDimensionsException(
                    "Image exceeds maximum pixel count of " + constraints.getMaxPixels() + ".");
        }

        AssetUploadPurpose resolved = purpose == null ? AssetUploadPurpose.GENERIC : purpose;
        if (resolved == AssetUploadPurpose.GENERIC) {
            return toDetectedUpload(detected);
        }

        switch (resolved) {
            case STAMP_ARTWORK -> validateMinAndSquare(
                    size,
                    1024,
                    1024,
                    STRICT_SQUARE_TOLERANCE,
                    "Stamp artwork must be at least 1024×1024.",
                    "Stamp artwork must be a 1:1 square ratio.");
            case STATION_COVER -> validateMinAndSquare(
                    size,
                    1024,
                    1024,
                    STRICT_SQUARE_TOLERANCE,
                    "Station cover image must be at least 1024×1024.",
                    "Station cover image must be a 1:1 square ratio.");
            case STAMP_PREVIEW -> validateMinAndSquare(
                    size,
                    512,
                    512,
                    NEAR_SQUARE_TOLERANCE,
                    "Stamp preview must be at least 512×512.",
                    "Stamp preview should be close to a 1:1 square ratio.");
            case STATION_CARD -> validateMinAndSquare(
                    size,
                    512,
                    512,
                    NEAR_SQUARE_TOLERANCE,
                    "Station card preview must be at least 512×512.",
                    "Station card preview should be close to a 1:1 square ratio.");
            case CAMPAIGN_THUMBNAIL -> validateMinAndSquare(
                    size,
                    512,
                    512,
                    NEAR_SQUARE_TOLERANCE,
                    "Campaign thumbnail must be at least 512×512.",
                    "Campaign thumbnail should be close to a 1:1 square ratio.");
            case MILESTONE_REWARD -> validateMinAndSquare(
                    size,
                    512,
                    512,
                    NEAR_SQUARE_TOLERANCE,
                    "Reward image must be at least 512×512.",
                    "Reward image should be close to a 1:1 square ratio.");
            case PARTNER_LOGO -> validateMinAndSquare(
                    size,
                    512,
                    512,
                    NEAR_SQUARE_TOLERANCE,
                    "Partner logo must be at least 512×512.",
                    "Partner logo should be close to a 1:1 square ratio.");
            case PARTNER_BANNER -> validateLandscapeBanner(
                    size,
                    "Banner image must be at least 1280×720.",
                    "Partner banner should use a 16:9 landscape ratio.");
            case CAMPAIGN_BANNER -> validateLandscapeBanner(
                    size,
                    "Banner image must be at least 1280×720.",
                    "Campaign banner should use a 16:9 landscape ratio.");
            default -> {
                // GENERIC already returned
            }
        }
        return toDetectedUpload(detected);
    }

    private static DetectedUpload toDetectedUpload(DetectedImage detected) {
        String ext = switch (detected.format()) {
            case JPEG -> "jpg";
            case PNG -> "png";
            case WEBP -> "webp";
        };
        return new DetectedUpload(detected.contentType(), ext);
    }

    /** Detected content type and extension after successful validation. */
    public record DetectedUpload(String contentType, String extension) {
    }

    private void sanitizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return;
        }
        String name = originalFilename.trim();
        if (name.contains("..") || name.contains("/") || name.contains("\\")) {
            throw new InvalidFileException("Filename contains illegal path characters");
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c < 0x20 || c == 0x7F) {
                throw new InvalidFileException("Filename contains illegal control characters");
            }
        }
        String lower = name.toLowerCase(Locale.ROOT);
        int lastDot = lower.lastIndexOf('.');
        if (lastDot > 0) {
            String trailing = lower.substring(lastDot + 1);
            int prevDot = lower.lastIndexOf('.', lastDot - 1);
            if (prevDot >= 0 && DANGEROUS_TRAILING_EXTENSIONS.contains(trailing)) {
                throw new InvalidFileException("Filename has a dangerous double extension");
            }
        }
    }

    private static byte[] readAllBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new InvalidFileException("Unable to read uploaded file");
        }
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        String normalized = contentType.trim().toLowerCase(Locale.ROOT);
        int semi = normalized.indexOf(';');
        if (semi >= 0) {
            normalized = normalized.substring(0, semi).trim();
        }
        if ("image/jpg".equals(normalized)) {
            return "image/jpeg";
        }
        return normalized;
    }

    private static DetectedImage detectByMagic(byte[] bytes) {
        if (startsWith(bytes, PNG_SIGNATURE)) {
            return new DetectedImage(ImageFormat.PNG, "image/png");
        }
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return new DetectedImage(ImageFormat.JPEG, "image/jpeg");
        }
        if (isWebp(bytes)) {
            return new DetectedImage(ImageFormat.WEBP, "image/webp");
        }
        throw new InvalidImageTypeException("Unrecognized or unsupported image format");
    }

    private static boolean isWebp(byte[] bytes) {
        if (bytes.length < 12) {
            return false;
        }
        return bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P';
    }

    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static ImageSize readPngDimensions(byte[] bytes) {
        // IHDR follows 8-byte signature + 4 length + 4 type
        if (bytes.length < 24) {
            throw new InvalidImageDimensionsException("Invalid PNG header");
        }
        if (!(bytes[12] == 'I' && bytes[13] == 'H' && bytes[14] == 'D' && bytes[15] == 'R')) {
            throw new InvalidImageDimensionsException("PNG missing IHDR chunk");
        }
        int width = readInt32Be(bytes, 16);
        int height = readInt32Be(bytes, 20);
        return new ImageSize(width, height);
    }

    private static ImageSize readJpegDimensions(byte[] bytes) {
        int i = 2;
        while (i + 3 < bytes.length) {
            if ((bytes[i] & 0xFF) != 0xFF) {
                i++;
                continue;
            }
            int marker = bytes[i + 1] & 0xFF;
            if (marker == 0xD8 || marker == 0xD9 || marker == 0x01
                    || (marker >= 0xD0 && marker <= 0xD7)) {
                i += 2;
                continue;
            }
            if (i + 4 >= bytes.length) {
                break;
            }
            int segmentLength = ((bytes[i + 2] & 0xFF) << 8) | (bytes[i + 3] & 0xFF);
            if (segmentLength < 2) {
                break;
            }
            // SOF0 / SOF2 (baseline / progressive)
            if ((marker == 0xC0 || marker == 0xC2) && i + 9 < bytes.length) {
                int height = ((bytes[i + 5] & 0xFF) << 8) | (bytes[i + 6] & 0xFF);
                int width = ((bytes[i + 7] & 0xFF) << 8) | (bytes[i + 8] & 0xFF);
                return new ImageSize(width, height);
            }
            i += 2 + segmentLength;
        }
        throw new InvalidImageDimensionsException("Unable to read JPEG dimensions");
    }

    private static ImageSize readWebpDimensions(byte[] bytes) {
        if (bytes.length < 30) {
            throw new InvalidImageDimensionsException("Invalid WebP header");
        }
        // bytes 12..15 = chunk FourCC
        if (bytes[12] == 'V' && bytes[13] == 'P' && bytes[14] == '8' && bytes[15] == 'X') {
            // VP8X: canvas size at offset 24 (24-bit LE width-1 / height-1)
            if (bytes.length < 30) {
                throw new InvalidImageDimensionsException("Invalid WebP VP8X header");
            }
            int width = 1 + ((bytes[24] & 0xFF) | ((bytes[25] & 0xFF) << 8) | ((bytes[26] & 0xFF) << 16));
            int height = 1 + ((bytes[27] & 0xFF) | ((bytes[28] & 0xFF) << 8) | ((bytes[29] & 0xFF) << 16));
            return new ImageSize(width, height);
        }
        if (bytes[12] == 'V' && bytes[13] == 'P' && bytes[14] == '8' && bytes[15] == ' ') {
            // VP8 lossy: frame header starts at offset 20; width/height at 26
            if (bytes.length < 30) {
                throw new InvalidImageDimensionsException("Invalid WebP VP8 header");
            }
            int width = ((bytes[26] & 0xFF) | ((bytes[27] & 0xFF) << 8)) & 0x3FFF;
            int height = ((bytes[28] & 0xFF) | ((bytes[29] & 0xFF) << 8)) & 0x3FFF;
            return new ImageSize(width, height);
        }
        if (bytes[12] == 'V' && bytes[13] == 'P' && bytes[14] == '8' && bytes[15] == 'L') {
            // VP8L: 5-byte signature then 14-bit width-1 / height-1 packed
            if (bytes.length < 25) {
                throw new InvalidImageDimensionsException("Invalid WebP VP8L header");
            }
            int b0 = bytes[21] & 0xFF;
            int b1 = bytes[22] & 0xFF;
            int b2 = bytes[23] & 0xFF;
            int b3 = bytes[24] & 0xFF;
            int width = 1 + (((b1 & 0x3F) << 8) | b0);
            int height = 1 + (((b3 & 0x0F) << 10) | (b2 << 2) | ((b1 & 0xC0) >> 6));
            return new ImageSize(width, height);
        }
        throw new InvalidImageDimensionsException("Unsupported WebP variant");
    }

    private static int readInt32Be(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24)
                | ((bytes[offset + 1] & 0xFF) << 16)
                | ((bytes[offset + 2] & 0xFF) << 8)
                | (bytes[offset + 3] & 0xFF);
    }

    private void validateLandscapeBanner(ImageSize size, String minMessage, String ratioMessage) {
        if (size.width() < 1280 || size.height() < 720) {
            throw new InvalidImageDimensionsException(minMessage);
        }
        double ratio = (double) size.width() / (double) size.height();
        if (Math.abs(ratio - TARGET_16_9) > LANDSCAPE_16_9_TOLERANCE * TARGET_16_9) {
            throw new InvalidImageDimensionsException(ratioMessage);
        }
    }

    private void validateMinAndSquare(
            ImageSize size,
            int minWidth,
            int minHeight,
            double tolerance,
            String minMessage,
            String ratioMessage) {
        if (size.width() < minWidth || size.height() < minHeight) {
            throw new InvalidImageDimensionsException(minMessage);
        }
        double ratio = (double) size.width() / (double) size.height();
        if (Math.abs(ratio - 1.0) > tolerance) {
            throw new InvalidImageDimensionsException(ratioMessage);
        }
    }

    private enum ImageFormat { JPEG, PNG, WEBP }

    private record DetectedImage(ImageFormat format, String contentType) {}

    private record ImageSize(int width, int height) {}
}
