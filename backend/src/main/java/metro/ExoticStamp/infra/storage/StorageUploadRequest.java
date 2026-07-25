package metro.ExoticStamp.infra.storage;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Structured upload request. Prefer this over legacy folder-string uploads.
 */
public record StorageUploadRequest(
        MultipartFile file,
        StorageObjectCategory category,
        StorageVisibility visibility,
        String entityId,
        String detectedContentType,
        String detectedExtension
) {
    public static StorageUploadRequest of(
            MultipartFile file,
            StorageObjectCategory category,
            StorageVisibility visibility,
            UUID entityId,
            String detectedContentType,
            String detectedExtension
    ) {
        return new StorageUploadRequest(
                file,
                category,
                visibility,
                entityId == null ? null : entityId.toString(),
                detectedContentType,
                detectedExtension
        );
    }

    public static StorageUploadRequest of(
            MultipartFile file,
            StorageObjectCategory category,
            StorageVisibility visibility,
            String entityId,
            String detectedContentType,
            String detectedExtension
    ) {
        return new StorageUploadRequest(
                file, category, visibility, entityId, detectedContentType, detectedExtension);
    }
}
