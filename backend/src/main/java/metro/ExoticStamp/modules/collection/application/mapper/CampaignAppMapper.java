package metro.ExoticStamp.modules.collection.application.mapper;

import metro.ExoticStamp.modules.collection.application.view.CampaignView;
import metro.ExoticStamp.modules.collection.application.view.StampDesignView;
import metro.ExoticStamp.modules.collection.application.view.StampPreviewView;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import org.springframework.stereotype.Component;

@Component
public class CampaignAppMapper {

    public CampaignView toCampaignView(Campaign c) {
        return CampaignView.builder()
                .id(c.getId())
                .code(c.getCode())
                .name(c.getName())
                .displayName(c.getDisplayName())
                .description(c.getDescription())
                .campaignType(c.getCampaignType() != null ? c.getCampaignType().name() : null)
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .startAt(c.getStartAt())
                .endAt(c.getEndAt())
                .bannerImageUrl(c.getBannerImageUrl())
                .thumbnailImageUrl(c.getThumbnailImageUrl())
                .priority(c.getPriority())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    public StampDesignView toStampDesignView(StampDesign s) {
        return StampDesignView.builder()
                .id(s.getId())
                .campaignId(s.getCampaignId())
                .stationId(s.getStationId())
                .name(s.getName())
                .description(s.getDescription())
                .imageUrl(s.getImageUrl())
                .previewImageUrl(s.getPreviewImageUrl())
                .rarity(s.getRarity() != null ? s.getRarity().name() : null)
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .sortOrder(s.getSortOrder())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }

    public StampPreviewView toStampPreviewView(StampDesign s) {
        if (s == null) {
            return null;
        }
        return StampPreviewView.builder()
                .id(s.getId())
                .name(s.getName())
                .imageUrl(s.getImageUrl())
                .previewImageUrl(s.getPreviewImageUrl())
                .rarity(s.getRarity() != null ? s.getRarity().name() : null)
                .build();
    }
}
