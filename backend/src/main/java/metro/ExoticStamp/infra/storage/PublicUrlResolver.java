package metro.ExoticStamp.infra.storage;

import org.springframework.stereotype.Component;

/**
 * Derives public URLs from object keys without coupling DB rows to a bucket hostname.
 */
@Component
public class PublicUrlResolver {

    private final StorageProperties storageProperties;

    public PublicUrlResolver(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public String toPublicUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            return objectKey;
        }
        String base = resolvePublicBase();
        if (base == null || base.isBlank()) {
            throw new IllegalStateException("storage.public-base-url (STORAGE_PUBLIC_BASE_URL) is required");
        }
        return trimTrailingSlash(base) + "/" + objectKey.replaceAll("^/+", "");
    }

    /**
     * Extract object key from a stored public URL when it matches the configured base.
     * Returns null for legacy external URLs that cannot be mapped.
     */
    public String tryExtractObjectKey(String urlOrKey) {
        if (urlOrKey == null || urlOrKey.isBlank()) {
            return null;
        }
        String value = urlOrKey.trim();
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            if (value.startsWith("public/") || value.startsWith("private/") || value.startsWith("temporary/")) {
                return value;
            }
            return null;
        }
        String base = resolvePublicBase();
        if (base == null || base.isBlank()) {
            return null;
        }
        String normalizedBase = trimTrailingSlash(base);
        if (value.startsWith(normalizedBase + "/")) {
            return value.substring(normalizedBase.length() + 1);
        }
        // Local provider may use storage.local.base-url
        String localBase = storageProperties.getLocal() != null
                ? trimTrailingSlash(storageProperties.getLocal().getBaseUrl())
                : null;
        if (localBase != null && !localBase.isBlank() && value.startsWith(localBase + "/")) {
            return value.substring(localBase.length() + 1);
        }
        return null;
    }

    private String resolvePublicBase() {
        if (storageProperties.getPublicBaseUrl() != null && !storageProperties.getPublicBaseUrl().isBlank()) {
            return storageProperties.getPublicBaseUrl();
        }
        if ("local".equalsIgnoreCase(storageProperties.getProvider())
                && storageProperties.getLocal() != null
                && storageProperties.getLocal().getBaseUrl() != null) {
            return storageProperties.getLocal().getBaseUrl();
        }
        return storageProperties.getPublicBaseUrl();
    }

    private static String trimTrailingSlash(String url) {
        if (url == null) {
            return "";
        }
        return url.replaceAll("/+$", "");
    }
}
