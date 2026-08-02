package metro.ExoticStamp.infra.storage.asset;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StoredAssetRepository extends JpaRepository<StoredAsset, UUID> {

    Optional<StoredAsset> findByObjectKey(String objectKey);

    Optional<StoredAsset> findByPublicUrl(String publicUrl);

    List<StoredAsset> findByStatusAndDeleteAfterBefore(
            StoredAssetStatus status, LocalDateTime before, Pageable pageable);

    @Query("""
            SELECT a FROM StoredAsset a
            WHERE a.status = metro.ExoticStamp.infra.storage.asset.StoredAssetStatus.PENDING
              AND a.createdAt < :before
            """)
    List<StoredAsset> findStalePending(@Param("before") LocalDateTime before, Pageable pageable);

    boolean existsByObjectKeyAndStatus(String objectKey, StoredAssetStatus status);
}
