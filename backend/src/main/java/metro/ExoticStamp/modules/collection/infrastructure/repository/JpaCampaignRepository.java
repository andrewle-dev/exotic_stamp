package metro.ExoticStamp.modules.collection.infrastructure.repository;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCampaignRepository extends JpaRepository<Campaign, UUID> {

    boolean existsByLineIdAndIsDefaultTrue(UUID lineId);

    @Query("""
            SELECT c FROM Campaign c
            WHERE c.lineId = :lineId AND c.isDefault = true AND c.status = 'ACTIVE' AND c.deletedAt IS NULL
            """)
    Optional<Campaign> findDefaultByLineId(@Param("lineId") UUID lineId);

    @Query("""
            SELECT c FROM Campaign c
            WHERE c.isDefault = true AND c.status = 'ACTIVE' AND c.deletedAt IS NULL
            """)
    List<Campaign> findAllActiveDefaults();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, UUID id);

    @Query("SELECT c FROM Campaign c WHERE c.deletedAt IS NULL")
    Page<Campaign> findAllNotDeleted(Pageable pageable);

    List<Campaign> findByLineIdOrderByCodeAsc(UUID lineId);

    @Query("""
            SELECT c FROM Campaign c
            WHERE c.status = 'ACTIVE' AND c.deletedAt IS NULL
              AND c.startAt <= :now AND c.endAt >= :now
            ORDER BY c.priority DESC, c.createdAt DESC
            """)
    List<Campaign> findActiveInWindow(@Param("now") LocalDateTime now);

    @Query("""
            SELECT DISTINCT c FROM Campaign c
            JOIN CampaignStationEntity cs ON cs.campaignId = c.id
            WHERE cs.stationId = :stationId
              AND c.status = 'ACTIVE' AND c.deletedAt IS NULL
              AND c.startAt <= :now AND c.endAt >= :now
            ORDER BY c.priority DESC, c.createdAt DESC
            """)
    List<Campaign> findActiveByStationId(@Param("stationId") UUID stationId, @Param("now") LocalDateTime now);
}
