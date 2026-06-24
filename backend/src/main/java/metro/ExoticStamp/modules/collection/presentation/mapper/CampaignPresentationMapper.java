package metro.ExoticStamp.modules.collection.presentation.mapper;

import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.command.CreateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.CreateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.command.UpdateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.UpdateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.view.ActiveCampaignStationView;
import metro.ExoticStamp.modules.collection.application.view.ActiveCampaignView;
import metro.ExoticStamp.modules.collection.application.view.CampaignStationView;
import metro.ExoticStamp.modules.collection.application.view.CampaignView;
import metro.ExoticStamp.modules.collection.application.view.StampDesignView;
import metro.ExoticStamp.modules.collection.application.view.StampPreviewView;
import metro.ExoticStamp.modules.collection.presentation.dto.request.CreateCampaignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.request.CreateStampDesignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.request.UpdateCampaignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.request.UpdateStampDesignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.response.ActiveCampaignListResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.ActiveCampaignResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.ActiveCampaignStationResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.CampaignResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.CampaignStationResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.StampDesignResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.StampPreviewResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CampaignPresentationMapper {

    public CreateCampaignCommand toCreateCommand(CreateCampaignRequest request) {
        return new CreateCampaignCommand(
                request.getCode(),
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                request.getCampaignType() != null ? request.getCampaignType().name() : null,
                request.getStartAt(),
                request.getEndAt(),
                request.getBannerImageUrl(),
                request.getThumbnailImageUrl(),
                request.getPriority()
        );
    }

    public UpdateCampaignCommand toUpdateCommand(UUID id, UpdateCampaignRequest request) {
        return new UpdateCampaignCommand(
                id,
                request.getCode(),
                request.getName(),
                request.getDisplayName(),
                request.getDescription(),
                request.getCampaignType() != null ? request.getCampaignType().name() : null,
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getStartAt(),
                request.getEndAt(),
                request.getBannerImageUrl(),
                request.getThumbnailImageUrl(),
                request.getPriority()
        );
    }

    public CreateStampDesignCommand toCreateCommand(CreateStampDesignRequest request) {
        return new CreateStampDesignCommand(
                request.getCampaignId(),
                request.getStationId(),
                request.getName(),
                request.getDescription(),
                request.getImageUrl(),
                request.getPreviewImageUrl(),
                request.getRarity() != null ? request.getRarity().name() : null,
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getSortOrder()
        );
    }

    public UpdateStampDesignCommand toUpdateCommand(UUID id, UpdateStampDesignRequest request) {
        return new UpdateStampDesignCommand(
                id,
                request.getCampaignId(),
                request.getStationId(),
                request.getName(),
                request.getDescription(),
                request.getImageUrl(),
                request.getPreviewImageUrl(),
                request.getRarity() != null ? request.getRarity().name() : null,
                request.getStatus() != null ? request.getStatus().name() : null,
                request.getSortOrder()
        );
    }

    public CampaignResponse toResponse(CampaignView view) {
        return CampaignResponse.builder()
                .id(view.id())
                .code(view.code())
                .name(view.name())
                .displayName(view.displayName())
                .description(view.description())
                .campaignType(view.campaignType())
                .status(view.status())
                .startAt(view.startAt())
                .endAt(view.endAt())
                .bannerImageUrl(view.bannerImageUrl())
                .thumbnailImageUrl(view.thumbnailImageUrl())
                .priority(view.priority())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public StampDesignResponse toResponse(StampDesignView view) {
        return StampDesignResponse.builder()
                .id(view.id())
                .campaignId(view.campaignId())
                .stationId(view.stationId())
                .name(view.name())
                .description(view.description())
                .imageUrl(view.imageUrl())
                .previewImageUrl(view.previewImageUrl())
                .rarity(view.rarity())
                .status(view.status())
                .sortOrder(view.sortOrder())
                .createdAt(view.createdAt())
                .updatedAt(view.updatedAt())
                .build();
    }

    public PageResponse<CampaignResponse> toCampaignPage(PageResult<CampaignView> page) {
        List<CampaignResponse> content = page.content().stream().map(this::toResponse).toList();
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.currentPage(), content.size());
    }

    public PageResponse<StampDesignResponse> toStampDesignPage(PageResult<StampDesignView> page) {
        List<StampDesignResponse> content = page.content().stream().map(this::toResponse).toList();
        return PageResponse.of(content, page.totalElements(), page.totalPages(), page.currentPage(), content.size());
    }

    public ActiveCampaignListResponse toActiveList(List<ActiveCampaignView> views) {
        return ActiveCampaignListResponse.builder()
                .campaigns(views.stream().map(this::toActiveResponse).toList())
                .build();
    }

    public ActiveCampaignResponse toActiveResponse(ActiveCampaignView view) {
        return ActiveCampaignResponse.builder()
                .id(view.id())
                .code(view.code())
                .name(view.name())
                .displayName(view.displayName())
                .description(view.description())
                .campaignType(view.campaignType())
                .bannerImageUrl(view.bannerImageUrl())
                .thumbnailImageUrl(view.thumbnailImageUrl())
                .priority(view.priority())
                .startAt(view.startAt())
                .endAt(view.endAt())
                .stations(view.stations().stream().map(this::toActiveStationResponse).toList())
                .build();
    }

    public CampaignStationResponse toResponse(CampaignStationView view) {
        return CampaignStationResponse.builder()
                .stationId(view.stationId())
                .name(view.name())
                .displayName(view.displayName())
                .lineId(view.lineId())
                .sortOrder(view.sortOrder())
                .build();
    }

    public ActiveCampaignStationResponse toActiveStationResponse(ActiveCampaignStationView view) {
        return ActiveCampaignStationResponse.builder()
                .id(view.id())
                .name(view.name())
                .displayName(view.displayName())
                .sortOrder(view.sortOrder())
                .stampPreview(toPreviewResponse(view.stampPreview()))
                .build();
    }

    private StampPreviewResponse toPreviewResponse(StampPreviewView view) {
        if (view == null) {
            return null;
        }
        return StampPreviewResponse.builder()
                .id(view.id())
                .name(view.name())
                .imageUrl(view.imageUrl())
                .previewImageUrl(view.previewImageUrl())
                .rarity(view.rarity())
                .build();
    }
}
