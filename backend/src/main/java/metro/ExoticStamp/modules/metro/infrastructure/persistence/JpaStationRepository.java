package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaStationRepository extends JpaRepository<Station, UUID> {

    List<Station> findAllByLineIdOrderBySortOrderAsc(UUID lineId);

    List<Station> findAllByLineIdAndStatusOrderBySortOrderAsc(UUID lineId, MetroStatus status);

    List<Station> findAllByOrderByLineIdAscSortOrderAsc();

    List<Station> findAllByStatusOrderByLineIdAscSortOrderAsc(MetroStatus status);

    @Query("""
            SELECT s FROM Station s JOIN Line l ON l.id = s.lineId
            WHERE s.status = metro.ExoticStamp.modules.metro.domain.model.MetroStatus.ACTIVE
              AND l.status = metro.ExoticStamp.modules.metro.domain.model.MetroStatus.ACTIVE
            ORDER BY s.lineId ASC, s.sortOrder ASC
            """)
    List<Station> findAllActiveUnderActiveLines();

    Optional<Station> findByNfcTagId(String nfcTagId);

    Optional<Station> findByQrCodeValue(String qrCodeValue);

    boolean existsByLineIdAndCode(UUID lineId, String code);

    boolean existsByLineIdAndCodeAndIdNot(UUID lineId, String code, UUID id);

    boolean existsByNfcTagId(String nfcTagId);

    boolean existsByQrCodeValue(String qrCodeValue);

    boolean existsByNfcTagIdAndIdNot(String nfcTagId, UUID id);

    boolean existsByQrCodeValueAndIdNot(String qrCodeValue, UUID id);

    boolean existsByLineIdAndSortOrder(UUID lineId, Integer sortOrder);

    boolean existsByLineIdAndSortOrderAndIdNot(UUID lineId, Integer sortOrder, UUID id);

    @Query(value = """
            SELECT s.id, s.name, l.name, s.collector_count
            FROM stations s
            JOIN lines l ON l.id = s.line_id
            ORDER BY s.collector_count DESC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findTop20StationStatsRaw();

    @Query("""
            SELECT s FROM Station s
            WHERE (:lineId IS NULL OR s.lineId = :lineId)
              AND (:status IS NULL OR s.status = :status)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(s.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(COALESCE(s.displayName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            """)
    Page<Station> search(@Param("lineId") UUID lineId, @Param("status") MetroStatus status,
                         @Param("search") String search, Pageable pageable);
}
