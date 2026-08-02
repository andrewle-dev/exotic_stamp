package metro.ExoticStamp.infra.storage;

import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Generates unique versioned object keys. Never embeds raw user filenames.
 */
@Component
public class ObjectKeyFactory {

    private static final Pattern SAFE_ID = Pattern.compile("^[A-Za-z0-9._-]{1,128}$");
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "png", "webp");

    private final Clock clock;

    public ObjectKeyFactory() {
        this(Clock.systemUTC());
    }

    public ObjectKeyFactory(Clock clock) {
        this.clock = clock;
    }

    public String generate(StorageUploadRequest request) {
        if (request == null || request.category() == null) {
            throw new InvalidFileException("Storage category is required");
        }
        String ext = normalizeExtension(request.detectedExtension(), request.detectedContentType());
        String uuid = UUID.randomUUID().toString();

        return switch (request.category()) {
            case STATION_COVER -> "public/stations/" + requireId(request.entityId(), "stationId")
                    + "/cover/" + uuid + "." + ext;
            case STAMP_DESIGN -> "public/stamp-designs/" + requireId(request.entityId(), "designId")
                    + "/" + uuid + "." + ext;
            case PARTNER_LOGO -> "public/partners/" + requireId(request.entityId(), "partnerId")
                    + "/logo/" + uuid + "." + ext;
            case PARTNER_BANNER -> "public/partners/" + requireId(request.entityId(), "partnerId")
                    + "/banner/" + uuid + "." + ext;
            case CAMPAIGN -> "public/campaigns/" + requireId(request.entityId(), "campaignId")
                    + "/" + uuid + "." + ext;
            case REWARD -> "public/rewards/" + requireId(request.entityId(), "milestoneId")
                    + "/" + uuid + "." + ext;
            case USER_PRIVATE -> "private/users/" + requireId(request.entityId(), "userId")
                    + "/" + uuid + "." + ext;
            case TEMPORARY, LEGACY_PUBLIC -> temporaryKey(uuid, ext);
        };
    }

    public boolean isPublicKey(String objectKey) {
        return objectKey != null && objectKey.startsWith("public/");
    }

    public boolean isPrivateKey(String objectKey) {
        return objectKey != null && objectKey.startsWith("private/");
    }

    public void assertSafeObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new InvalidFileException("Object key is required");
        }
        String normalized = objectKey.replace('\\', '/');
        if (normalized.contains("..") || normalized.startsWith("/") || normalized.contains("//")) {
            throw new InvalidFileException("Invalid object key");
        }
        if (!(normalized.startsWith("public/")
                || normalized.startsWith("private/")
                || normalized.startsWith("temporary/"))) {
            throw new InvalidFileException("Object key prefix not allowed");
        }
    }

    private String temporaryKey(String uuid, String ext) {
        LocalDate today = LocalDate.now(clock);
        // Staged business media must remain under public/* for bucket public-read policy.
        return String.format(
                Locale.ROOT,
                "public/temporary/%04d/%02d/%02d/%s.%s",
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                uuid,
                ext);
    }

    private static String requireId(String entityId, String label) {
        if (entityId == null || entityId.isBlank()) {
            throw new InvalidFileException(label + " is required for object key");
        }
        String trimmed = entityId.trim();
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\")) {
            throw new InvalidFileException("Invalid " + label);
        }
        if (!SAFE_ID.matcher(trimmed).matches()) {
            throw new InvalidFileException("Invalid " + label);
        }
        return trimmed;
    }

    static String normalizeExtension(String extension, String contentType) {
        if (extension != null && !extension.isBlank()) {
            String ext = extension.trim().toLowerCase(Locale.ROOT).replace(".", "");
            if ("jpeg".equals(ext)) {
                ext = "jpg";
            }
            if (ALLOWED_EXT.contains(ext)) {
                return ext;
            }
        }
        return extensionForContentType(contentType);
    }

    public static String extensionForContentType(String contentType) {
        if (contentType == null) {
            throw new InvalidFileException("Content type is required for object key");
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new InvalidFileException("Unsupported content type for object key");
        };
    }

    public static String contentTypeForExtension(String ext) {
        return switch (ext.toLowerCase(Locale.ROOT)) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    public static String cacheControlFor(StorageVisibility visibility) {
        if (visibility == StorageVisibility.PRIVATE) {
            return "private, no-store";
        }
        // Versioned keys are immutable; long cache is safe.
        return "public, max-age=31536000, immutable";
    }
}
