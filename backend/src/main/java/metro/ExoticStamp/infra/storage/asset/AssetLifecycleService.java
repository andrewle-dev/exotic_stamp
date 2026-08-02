package metro.ExoticStamp.infra.storage.asset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.infra.storage.PublicUrlResolver;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Tracks stored object metadata and orphan lifecycle. PutObject and DB commit are not atomic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetLifecycleService {

    private final StoredAssetRepository storedAssetRepository;
    private final StorageProperties storageProperties;
    private final PublicUrlResolver publicUrlResolver;
    private final StorageMetrics storageMetrics;

    /**
     * Persist PENDING metadata in a separate transaction so it survives caller rollback
     * after a successful storage write.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public StoredAsset recordPending(StorageUploadResult result, String entityType, UUID entityId) {
        StoredAsset asset = StoredAsset.builder()
                .id(UUID.randomUUID())
                .provider(result.provider())
                .objectKey(result.objectKey())
                .contentType(result.contentType())
                .byteSize(result.byteSize())
                .checksum(result.checksum())
                .visibility(result.visibility())
                .status(StoredAssetStatus.PENDING)
                .entityType(entityType)
                .entityId(entityId)
                .publicUrl(result.publicUrl())
                .createdAt(LocalDateTime.now())
                .build();
        return storedAssetRepository.save(asset);
    }

    @Transactional
    public void activate(UUID assetId) {
        StoredAsset asset = storedAssetRepository.findById(assetId)
                .orElseThrow(() -> new IllegalStateException("Stored asset not found: " + assetId));
        asset.setStatus(StoredAssetStatus.ACTIVE);
        storedAssetRepository.save(asset);
    }

    /**
     * After a successful pointer change, mark the previous object ORPHANED (do not delete yet).
     */
    @Transactional
    public void orphanPrevious(String previousUrlOrKey, DurationOrphanDefaults defaults) {
        if (previousUrlOrKey == null || previousUrlOrKey.isBlank()) {
            return;
        }
        Optional<StoredAsset> existing = storedAssetRepository.findByPublicUrl(previousUrlOrKey);
        if (existing.isEmpty()) {
            String key = publicUrlResolver.tryExtractObjectKey(previousUrlOrKey);
            if (key != null) {
                existing = storedAssetRepository.findByObjectKey(key);
            }
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deleteAfter = now.plus(storageProperties.getCleanup().getOrphanRetention());
        if (existing.isPresent()) {
            StoredAsset asset = existing.get();
            if (asset.getStatus() == StoredAssetStatus.ORPHANED) {
                return;
            }
            asset.setStatus(StoredAssetStatus.ORPHANED);
            asset.setOrphanedAt(now);
            asset.setDeleteAfter(deleteAfter);
            storedAssetRepository.save(asset);
            storageMetrics.recordOrphanCreated();
            return;
        }
        // Legacy URL with no metadata row — create orphan tracking record when key is known.
        String key = publicUrlResolver.tryExtractObjectKey(previousUrlOrKey);
        if (key == null) {
            log.info("[AssetLifecycle] legacy URL orphan without extractable key; skip hard tracking");
            return;
        }
        StoredAsset orphan = StoredAsset.builder()
                .id(UUID.randomUUID())
                .provider(storageProperties.getProvider())
                .objectKey(key)
                .visibility(key.startsWith("private/") ? StorageVisibility.PRIVATE : StorageVisibility.PUBLIC)
                .status(StoredAssetStatus.ORPHANED)
                .publicUrl(previousUrlOrKey.startsWith("http") ? previousUrlOrKey : publicUrlResolver.toPublicUrl(key))
                .createdAt(now)
                .orphanedAt(now)
                .deleteAfter(deleteAfter)
                .build();
        storedAssetRepository.save(orphan);
        storageMetrics.recordOrphanCreated();
        if (defaults != null) {
            // placeholder for future entity-type defaults
        }
    }

    @Transactional
    public void markMissingReferenced(String objectKey) {
        storageMetrics.recordMissingReferenced();
        log.warn("[AssetLifecycle] ACTIVE referenced object missing keyPrefix={}", keyPrefix(objectKey));
    }

    private static String keyPrefix(String objectKey) {
        if (objectKey == null) {
            return "null";
        }
        int slash = objectKey.lastIndexOf('/');
        return slash > 0 ? objectKey.substring(0, slash) : objectKey;
    }

    /** Marker type reserved for future entity-specific orphan defaults. */
    public record DurationOrphanDefaults() {
    }
}
