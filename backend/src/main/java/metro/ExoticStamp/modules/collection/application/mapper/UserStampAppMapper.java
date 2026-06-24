package metro.ExoticStamp.modules.collection.application.mapper;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserStampAppMapper {

    public UserStampView toUserStampResponse(
            UserStamp userStamp,
            MetroStationView station,
            StampDesign stampDesign
    ) {
        return UserStampView.builder()
                .stampId(userStamp.getId())
                .stationId(userStamp.getStationId())
                .lineId(station.lineId())
                .campaignId(userStamp.getCampaignId())
                .stationName(station.name())
                .stampDesignUrl(stampDesign != null ? stampDesign.getImageUrl() : null)
                .collectedAt(userStamp.getCollectedAt())
                .collectMethod(userStamp.getCollectMethod() != null ? userStamp.getCollectMethod().name() : null)
                .build();
    }

    public CollectMethod resolveCollectMethod(String nfcTagId, String qrToken) {
        if (nfcTagId != null && !nfcTagId.isBlank()) return CollectMethod.NFC;
        return CollectMethod.QR;
    }
}

