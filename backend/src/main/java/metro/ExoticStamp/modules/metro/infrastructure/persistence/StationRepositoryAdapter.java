package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.MetroPageResult;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StationRepositoryAdapter implements StationRepository {

    private final JpaStationRepository jpaStationRepository;
    private final EntityManager entityManager;

    @Override
    public Optional<Station> findById(UUID id) {
        return jpaStationRepository.findById(id);
    }

    @Override
    public List<Station> findAllByIdIn(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return jpaStationRepository.findAllById(ids);
    }

    @Override
    public Station save(Station station) {
        return jpaStationRepository.save(station);
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public List<Station> findAllByLineId(UUID lineId) {
        return jpaStationRepository.findAllByLineIdOrderBySortOrderAsc(lineId);
    }

    @Override
    public List<Station> findAllByLineIdAndStatus(UUID lineId, MetroStatus status) {
        return jpaStationRepository.findAllByLineIdAndStatusOrderBySortOrderAsc(lineId, status);
    }

    @Override
    public List<Station> findAllStationsOrdered() {
        return jpaStationRepository.findAllByOrderByLineIdAscSortOrderAsc();
    }

    @Override
    public List<Station> findAllByStatus(MetroStatus status) {
        return jpaStationRepository.findAllByStatusOrderByLineIdAscSortOrderAsc(status);
    }

    @Override
    public List<Station> findAllActiveUnderActiveLines() {
        return jpaStationRepository.findAllActiveUnderActiveLines();
    }

    @Override
    public Optional<Station> findByNfcTagId(String nfcTagId) {
        return jpaStationRepository.findByNfcTagId(nfcTagId);
    }

    @Override
    public Optional<Station> findByQrCodeValue(String qrCodeValue) {
        return jpaStationRepository.findByQrCodeValue(qrCodeValue);
    }

    @Override
    public boolean existsByLineIdAndCode(UUID lineId, String code) {
        return jpaStationRepository.existsByLineIdAndCode(lineId, code);
    }

    @Override
    public boolean existsByLineIdAndCodeAndIdNot(UUID lineId, String code, UUID id) {
        return jpaStationRepository.existsByLineIdAndCodeAndIdNot(lineId, code, id);
    }

    @Override
    public boolean existsByNfcTagId(String nfcTagId) {
        return jpaStationRepository.existsByNfcTagId(nfcTagId);
    }

    @Override
    public boolean existsByQrCodeValue(String qrCodeValue) {
        return jpaStationRepository.existsByQrCodeValue(qrCodeValue);
    }

    @Override
    public boolean existsByNfcTagIdAndIdNot(String nfcTagId, UUID id) {
        return jpaStationRepository.existsByNfcTagIdAndIdNot(nfcTagId, id);
    }

    @Override
    public boolean existsByQrCodeValueAndIdNot(String qrCodeValue, UUID id) {
        return jpaStationRepository.existsByQrCodeValueAndIdNot(qrCodeValue, id);
    }

    @Override
    public boolean existsByLineIdAndSortOrder(UUID lineId, Integer sortOrder) {
        return jpaStationRepository.existsByLineIdAndSortOrder(lineId, sortOrder);
    }

    @Override
    public boolean existsByLineIdAndSortOrderAndIdNot(UUID lineId, Integer sortOrder, UUID id) {
        return jpaStationRepository.existsByLineIdAndSortOrderAndIdNot(lineId, sortOrder, id);
    }

    @Override
    public List<Object[]> findTop20StationStatsRaw() {
        return jpaStationRepository.findTop20StationStatsRaw();
    }

    @Override
    public MetroPageResult<Station> search(UUID lineId, MetroStatus status, String search, int page, int size,
                                           String sortField, boolean ascending) {
        int effectiveSize = size <= 0 ? 20 : Math.min(size, 100);
        Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, mapSortField(sortField));
        PageRequest pageable = PageRequest.of(page, effectiveSize, sort);
        Page<Station> result = jpaStationRepository.search(lineId, status, blankToNull(search), pageable);
        return new MetroPageResult<>(result.getContent(), result.getTotalElements(), result.getTotalPages(),
                result.getNumber(), result.getSize());
    }

    private static String mapSortField(String sortField) {
        if (sortField == null || sortField.isBlank()) {
            return "sortOrder";
        }
        return switch (sortField) {
            case "code" -> "code";
            case "name" -> "name";
            case "status" -> "status";
            case "createdAt" -> "createdAt";
            default -> "sortOrder";
        };
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
