package metro.ExoticStamp.modules.collection.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.exception.StampDesignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StampDesignResolver {

    private final StampDesignRepository stampDesignRepository;

    public StampDesign resolveActive(UUID campaignId, UUID stationId) {
        return stampDesignRepository.findActiveByCampaignIdAndStationId(campaignId, stationId)
                .orElseThrow(() -> new StampDesignNotFoundException(campaignId, stationId));
    }
}
