package metro.ExoticStamp.modules.metro.infrastructure.persistence;

import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaLineRepository extends JpaRepository<Line, UUID> {

    List<Line> findAllByStatusOrderBySortOrderAsc(MetroStatus status);

    Page<Line> findAllByStatus(MetroStatus status, Pageable pageable);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    Optional<Line> findByCode(String code);

    @Query("""
            SELECT l FROM Line l
            WHERE (:status IS NULL OR l.status = :status)
              AND (:search IS NULL OR :search = ''
                   OR LOWER(l.code) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(l.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                   OR LOWER(COALESCE(l.displayName, '')) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
            """)
    Page<Line> search(@Param("status") MetroStatus status, @Param("search") String search, Pageable pageable);
}
