package metro.ExoticStamp.modules.collection.domain.exception;

import metro.ExoticStamp.common.exceptions.DomainException;

import java.util.UUID;

public class CampaignStationNotEligibleException extends DomainException {

    public CampaignStationNotEligibleException(UUID campaignId, UUID stationId) {
        super("Station " + stationId + " is not assigned to campaign " + campaignId);
    }
}
