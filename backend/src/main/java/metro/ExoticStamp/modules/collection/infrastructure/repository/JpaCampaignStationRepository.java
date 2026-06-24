package metro.ExoticStamp.modules.collection.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaCampaignStationRepository extends JpaRepository<CampaignStationEntity, UUID> {

    boolean existsByCampaignIdAndStationId(UUID campaignId, UUID stationId);

    void deleteByCampaignIdAndStationId(UUID campaignId, UUID stationId);

    List<CampaignStationEntity> findByCampaignId(UUID campaignId);

    @Query("SELECT COUNT(cs) FROM CampaignStationEntity cs WHERE cs.campaignId = :campaignId")
    int countByCampaignId(@Param("campaignId") UUID campaignId);
}
