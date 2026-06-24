package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.support.CampaignAuditHelper;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidStationException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.service.CampaignDomainService;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignStationCommandService {

    private final CampaignRepository campaignRepository;
    private final CampaignStationRepository campaignStationRepository;
    private final CampaignDomainService campaignDomainService;
    private final StationReadPort stationReadPort;
    private final LineReadPort lineReadPort;
    private final CampaignAuditHelper campaignAuditHelper;

    @Transactional
    public void assign(UUID campaignId, UUID stationId) {
        Campaign campaign = campaignRepository.findByIdNotDeleted(campaignId)
                .orElseThrow(() -> new CampaignNotFoundException(campaignId));
        campaignDomainService.assertMutable(campaign);

        MetroStationView station = stationReadPort.getStationViewById(stationId);
        if (!station.active()) {
            throw new InvalidStationException(stationId, "Station is not active");
        }
        var line = lineReadPort.getLineById(station.lineId());
        if (!line.active()) {
            throw new InvalidStationException(stationId, "Line is not active");
        }

        campaignStationRepository.assign(campaignId, stationId);
        campaignAuditHelper.scheduleStationAssigned(campaignId, stationId);
    }

    @Transactional
    public void remove(UUID campaignId, UUID stationId) {
        Campaign campaign = campaignRepository.findByIdNotDeleted(campaignId)
                .orElseThrow(() -> new CampaignNotFoundException(campaignId));
        campaignDomainService.assertMutable(campaign);
        campaignStationRepository.remove(campaignId, stationId);
        campaignAuditHelper.scheduleStationRemoved(campaignId, stationId);
    }
}
