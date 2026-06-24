package metro.ExoticStamp.modules.collection.application.mapper;

import metro.ExoticStamp.modules.collection.application.view.AdminCampaignView;
import metro.ExoticStamp.modules.collection.application.view.AdminStampDesignView;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import org.springframework.stereotype.Component;

@Component
public class CollectionAdminMapper {

    public AdminCampaignView toCampaignView(Campaign c) {
        return AdminCampaignView.builder()
                .id(c.getId())
                .lineId(c.getLineId())
                .partnerId(c.getPartnerId())
                .code(c.getCode())
                .name(c.getName())
                .description(c.getDescription())
                .bannerUrl(c.getBannerImageUrl())
                .startDate(c.getStartAt())
                .endDate(c.getEndAt())
                .active(c.isActiveForCollection())
                .defaultCampaign(c.isDefault())
                .build();
    }

    public AdminStampDesignView toStampDesignView(StampDesign s) {
        return AdminStampDesignView.builder()
                .id(s.getId())
                .stationId(s.getStationId())
                .campaignId(s.getCampaignId())
                .name(s.getName())
                .artworkUrl(s.getImageUrl())
                .animationUrl(s.getAnimationUrl())
                .soundUrl(s.getSoundUrl())
                .limited(s.isLimited())
                .active(s.isActiveDesign())
                .build();
    }
}
