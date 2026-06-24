package metro.ExoticStamp.modules.collection.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignStationDuplicateException;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CampaignStationRepositoryAdapter implements CampaignStationRepository {

    private final JpaCampaignStationRepository jpaCampaignStationRepository;

    @Override
    public void assign(UUID campaignId, UUID stationId) {
        if (jpaCampaignStationRepository.existsByCampaignIdAndStationId(campaignId, stationId)) {
            throw new CampaignStationDuplicateException();
        }
        try {
            jpaCampaignStationRepository.save(CampaignStationEntity.builder()
                    .campaignId(campaignId)
                    .stationId(stationId)
                    .build());
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateAssignment(ex)) {
                throw new CampaignStationDuplicateException();
            }
            throw ex;
        }
    }

    @Override
    public void remove(UUID campaignId, UUID stationId) {
        jpaCampaignStationRepository.deleteByCampaignIdAndStationId(campaignId, stationId);
    }

    @Override
    public boolean exists(UUID campaignId, UUID stationId) {
        return jpaCampaignStationRepository.existsByCampaignIdAndStationId(campaignId, stationId);
    }

    @Override
    public List<UUID> findStationIdsByCampaignId(UUID campaignId) {
        return jpaCampaignStationRepository.findByCampaignId(campaignId).stream()
                .map(CampaignStationEntity::getStationId)
                .toList();
    }

    @Override
    public int countByCampaignId(UUID campaignId) {
        return jpaCampaignStationRepository.countByCampaignId(campaignId);
    }

    private static boolean isDuplicateAssignment(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_campaign_stations");
    }
}
