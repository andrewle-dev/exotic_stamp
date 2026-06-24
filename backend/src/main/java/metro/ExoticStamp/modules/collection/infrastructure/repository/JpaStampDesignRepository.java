package metro.ExoticStamp.modules.collection.infrastructure.repository;

import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaStampDesignRepository extends JpaRepository<StampDesign, UUID> {

    @Query("""
            SELECT sd FROM StampDesign sd
            WHERE sd.campaignId = :campaignId AND sd.stationId = :stationId
              AND sd.status = 'ACTIVE' AND sd.deletedAt IS NULL
            """)
    Optional<StampDesign> findActiveByCampaignIdAndStationId(
            @Param("campaignId") UUID campaignId,
            @Param("stationId") UUID stationId);

    @Query("""
            SELECT sd FROM StampDesign sd
            WHERE sd.campaignId = :campaignId AND sd.stationId IN :stationIds
              AND sd.status = 'ACTIVE' AND sd.deletedAt IS NULL
            """)
    List<StampDesign> findActiveByCampaignIdAndStationIdIn(
            @Param("campaignId") UUID campaignId,
            @Param("stationIds") Collection<UUID> stationIds);

    @Query("""
            SELECT CASE WHEN COUNT(sd) > 0 THEN true ELSE false END FROM StampDesign sd
            WHERE sd.campaignId = :campaignId AND sd.stationId = :stationId
              AND sd.status = 'ACTIVE' AND sd.deletedAt IS NULL
            """)
    boolean existsActiveByCampaignIdAndStationId(
            @Param("campaignId") UUID campaignId,
            @Param("stationId") UUID stationId);

    @Query("""
            SELECT CASE WHEN COUNT(sd) > 0 THEN true ELSE false END FROM StampDesign sd
            WHERE sd.campaignId = :campaignId AND sd.stationId = :stationId
              AND sd.status = 'ACTIVE' AND sd.deletedAt IS NULL
              AND sd.id <> :excludeId
            """)
    boolean existsActiveByCampaignIdAndStationIdAndIdNot(
            @Param("campaignId") UUID campaignId,
            @Param("stationId") UUID stationId,
            @Param("excludeId") UUID excludeId);

    @Query("""
            SELECT sd FROM StampDesign sd
            WHERE sd.campaignId = :campaignId AND sd.deletedAt IS NULL
            ORDER BY sd.sortOrder ASC, sd.name ASC
            """)
    List<StampDesign> findByCampaignIdOrderBySortOrderAsc(@Param("campaignId") UUID campaignId);

    @Query("SELECT sd FROM StampDesign sd WHERE sd.deletedAt IS NULL")
    Page<StampDesign> findAllNotDeleted(Pageable pageable);
}
