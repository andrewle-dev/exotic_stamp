package metro.ExoticStamp.infra.storage;

/**
 * Result of a successful PutObject / local write. {@code publicUrl} is derived from
 * configured public base URL + object key for public objects; null for private.
 */
public record StorageUploadResult(
        String objectKey,
        String publicUrl,
        String contentType,
        long byteSize,
        String checksum,
        StorageVisibility visibility,
        String provider
) {
}
