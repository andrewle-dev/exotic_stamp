package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import metro.ExoticStamp.modules.metro.domain.repository.StationScanKeyRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StationScanKeyRepositoryAdapter implements StationScanKeyRepository {

    private final JpaStationScanKeyRepository jpaStationScanKeyRepository;

    @Override
    public StationScanKey save(StationScanKey scanKey) {
        return jpaStationScanKeyRepository.save(scanKey);
    }

    @Override
    public Optional<StationScanKey> findById(UUID id) {
        return jpaStationScanKeyRepository.findById(id);
    }

    @Override
    public List<StationScanKey> findAllByStationIdOrderByCreatedAtDesc(UUID stationId) {
        return jpaStationScanKeyRepository.findAllByStationIdOrderByCreatedAtDesc(stationId);
    }

    @Override
    public Optional<StationScanKey> findByKeyHash(String keyHash) {
        return jpaStationScanKeyRepository.findByKeyHash(keyHash);
    }

    @Override
    public Optional<StationScanKey> findByKeyHashAndStatus(String keyHash, ScanKeyStatus status) {
        return jpaStationScanKeyRepository.findByKeyHashAndStatus(keyHash, status);
    }

    @Override
    public Optional<StationScanKey> findByKeyHashAndScanTypeAndStatus(
            String keyHash, ScanType scanType, ScanKeyStatus status) {
        return jpaStationScanKeyRepository.findByKeyHashAndScanTypeAndStatus(keyHash, scanType, status);
    }

    @Override
    public boolean existsByStationIdAndStatus(UUID stationId, ScanKeyStatus status) {
        return jpaStationScanKeyRepository.existsByStationIdAndStatus(stationId, status);
    }

    @Override
    public void updateLastSeenAt(UUID id, LocalDateTime lastSeenAt) {
        jpaStationScanKeyRepository.updateLastSeenAt(id, lastSeenAt);
    }
}
