package metro.ExoticStamp.modules.collection.domain.service;

import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StampDesignDomainService {

    private final CampaignStationRepository campaignStationRepository;

    public StampDesignDomainService(CampaignStationRepository campaignStationRepository) {
        this.campaignStationRepository = campaignStationRepository;
    }

    public void assertStationInCampaign(UUID campaignId, UUID stationId) {
        if (stationId == null) {
            throw new InvalidRequestException("stationId is required");
        }
        if (campaignId == null) {
            throw new InvalidRequestException("campaignId is required");
        }
        if (!campaignStationRepository.exists(campaignId, stationId)) {
            throw new InvalidRequestException("Station does not belong to campaign");
        }
    }
}
