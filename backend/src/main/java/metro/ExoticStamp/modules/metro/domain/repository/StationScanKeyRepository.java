package metro.ExoticStamp.modules.metro.domain.repository;

import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StationScanKeyRepository {

    StationScanKey save(StationScanKey scanKey);

    Optional<StationScanKey> findById(UUID id);

    List<StationScanKey> findAllByStationIdOrderByCreatedAtDesc(UUID stationId);

    Optional<StationScanKey> findByKeyHash(String keyHash);

    Optional<StationScanKey> findByKeyHashAndStatus(String keyHash, ScanKeyStatus status);

    Optional<StationScanKey> findByKeyHashAndScanTypeAndStatus(
            String keyHash, ScanType scanType, ScanKeyStatus status);

    boolean existsByStationIdAndStatus(UUID stationId, ScanKeyStatus status);

    void updateLastSeenAt(UUID id, LocalDateTime lastSeenAt);
}
