package metro.ExoticStamp.infra.storage;

import metro.ExoticStamp.common.exceptions.storage.FileTooLargeException;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageDimensionsException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@Component
public class FileValidator {

    /** Tight tolerance for strict 1:1 artwork (≈2%). */
    private static final double STRICT_SQUARE_TOLERANCE = 0.02;
    /** Soft tolerance for logos / thumbs near 1:1 (≈5%). */
    private static final double NEAR_SQUARE_TOLERANCE = 0.05;
    /** Soft tolerance for landscape banners near 16:9 (≈5%). */
    private static final double LANDSCAPE_16_9_TOLERANCE = 0.05;
    private static final double TARGET_16_9 = 16.0 / 9.0;

    private final StorageProperties storageProperties;

    public FileValidator(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public void validate(MultipartFile file) {
        validate(file, AssetUploadPurpose.GENERIC);
    }

    public void validate(MultipartFile file, AssetUploadPurpose purpose) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidImageTypeException("Content type is missing");
        }
        if (!storageProperties.getFile().getAllowedTypes().contains(contentType)) {
            throw new InvalidImageTypeException("Unsupported image type: " + contentType);
        }
        long maxBytes = storageProperties.getFile().getMaxSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(
                    "File exceeds maximum size of " + storageProperties.getFile().getMaxSizeMb() + " MB");
        }

        AssetUploadPurpose resolved = purpose == null ? AssetUploadPurpose.GENERIC : purpose;
        if (resolved == AssetUploadPurpose.GENERIC) {
            return;
        }

        ImageSize size = readImageSize(file);
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

    private ImageSize readImageSize(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new InvalidImageDimensionsException(
                        "Unable to read image dimensions. Use PNG or JPEG.");
            }
            return new ImageSize(image.getWidth(), image.getHeight());
        } catch (InvalidImageDimensionsException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new InvalidImageDimensionsException(
                    "Unable to read image dimensions. Use PNG or JPEG.");
        }
    }

    private record ImageSize(int width, int height) {}
}
