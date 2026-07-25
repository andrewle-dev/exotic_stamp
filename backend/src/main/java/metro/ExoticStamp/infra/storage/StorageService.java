package metro.ExoticStamp.infra.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * Object storage port. Implementations: local filesystem (dev/test) and Amazon S3 (prod).
 */
public interface StorageService {

    /**
     * Upload using a generated versioned object key. Validation must already have completed.
     */
    StorageUploadResult upload(StorageUploadRequest request);

    /**
     * Legacy folder upload — maps to temporary/public keys. Prefer {@link #upload(StorageUploadRequest)}.
     *
     * @param file   multipart file
     * @param folder logical folder hint (not embedded as raw user path)
     * @return public URL
     */
    default String upload(MultipartFile file, String folder) {
        String contentType = file.getContentType();
        String ext = ObjectKeyFactory.extensionForContentType(contentType);
        StorageUploadRequest request = StorageUploadRequest.of(
                file,
                StorageObjectCategory.LEGACY_PUBLIC,
                StorageVisibility.PUBLIC,
                (String) null,
                contentType,
                ext
        );
        return upload(request).publicUrl();
    }

    /**
     * Hard-delete by object key or public URL. Missing objects are ignored.
     * Prefer orphan marking for business replace flows.
     */
    void delete(String fileUrlOrObjectKey);

    /**
     * Head/metadata existence check (bounded). Returns false when object is missing.
     */
    boolean exists(String objectKey);

    /**
     * Create a short-lived presigned GET URL for a private object key.
     * Callers must authorize ownership before invoking.
     */
    String createPresignedGetUrl(String objectKey);
}
