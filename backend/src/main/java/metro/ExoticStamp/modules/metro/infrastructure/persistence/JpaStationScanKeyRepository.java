package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaStationScanKeyRepository extends JpaRepository<StationScanKey, UUID> {

    List<StationScanKey> findAllByStationIdOrderByCreatedAtDesc(UUID stationId);

    Optional<StationScanKey> findByKeyHash(String keyHash);

    Optional<StationScanKey> findByKeyHashAndStatus(String keyHash, ScanKeyStatus status);

    Optional<StationScanKey> findByKeyHashAndScanTypeAndStatus(
            String keyHash, ScanType scanType, ScanKeyStatus status);

    boolean existsByStationIdAndStatus(UUID stationId, ScanKeyStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE StationScanKey k SET k.lastSeenAt = :lastSeenAt, k.updatedAt = :lastSeenAt WHERE k.id = :id")
    int updateLastSeenAt(@Param("id") UUID id, @Param("lastSeenAt") LocalDateTime lastSeenAt);
}
