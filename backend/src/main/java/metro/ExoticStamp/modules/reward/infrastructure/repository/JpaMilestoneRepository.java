package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaMilestoneRepository extends JpaRepository<Milestone, UUID> {

    @Query("""
            SELECT m FROM Milestone m
            WHERE m.campaignId = :campaignId
              AND m.status = 'ACTIVE'
              AND m.deletedAt IS NULL
            ORDER BY m.stampsRequired ASC, m.sortOrder ASC
            """)
    List<Milestone> findActiveByCampaignId(@Param("campaignId") UUID campaignId);

    @Query("""
            SELECT m FROM Milestone m
            WHERE m.active = true
            AND (m.lineId IS NULL OR m.lineId = :lineId)
            AND (m.campaignId IS NULL OR m.campaignId = :campaignId)
            ORDER BY m.stampsRequired ASC
            """)
    List<Milestone> findActiveApplicableToLineAndCampaign(
            @Param("lineId") UUID lineId,
            @Param("campaignId") UUID campaignId
    );

    @Query("""
            SELECT m FROM Milestone m
            WHERE m.deletedAt IS NULL
              AND m.campaignId = :campaignId
            ORDER BY m.sortOrder ASC, m.stampsRequired ASC
            """)
    List<Milestone> findAllByCampaignIdOrderBySortOrderAsc(@Param("campaignId") UUID campaignId);

    @Query("""
            SELECT m FROM Milestone m
            WHERE m.deletedAt IS NULL
              AND (:campaignId IS NULL OR m.campaignId = :campaignId)
              AND (:status IS NULL OR m.status = :status)
            ORDER BY m.sortOrder ASC, m.stampsRequired ASC
            """)
    Page<Milestone> findAllNotDeleted(
            @Param("campaignId") UUID campaignId,
            @Param("status") MilestoneStatus status,
            Pageable pageable
    );

    Optional<Milestone> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByCampaignIdAndCodeAndIdNotAndDeletedAtIsNull(UUID campaignId, String code, UUID excludeId);

    Page<Milestone> findByActive(boolean active, Pageable pageable);
}
