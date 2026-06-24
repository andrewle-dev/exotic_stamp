package metro.ExoticStamp.modules.collection.presentation.mapper;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.StampCollectView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampBookResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampCollectResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectionResponseMapper {

    public StampCollectResponse toResponse(StampCollectView view) {
        return StampCollectResponse.builder()
                .stampId(view.stampId())
                .stationId(view.stationId())
                .stationName(view.stationName())
                .lineId(view.lineId())
                .campaignId(view.campaignId())
                .stampDesignUrl(view.stampDesignUrl())
                .collectedAt(view.collectedAt())
                .isNew(view.isNew())
                .collectMethod(view.collectMethod())
                .progress(toResponse(view.progress()))
                .build();
    }

    public ProgressResponse toResponse(ProgressView view) {
        return ProgressResponse.builder()
                .lineId(view.lineId())
                .collected(view.collected())
                .total(view.total())
                .percentage(view.percentage())
                .build();
    }

    public UserStampResponse toResponse(UserStampView view) {
        return UserStampResponse.builder()
                .stampId(view.stampId())
                .stationId(view.stationId())
                .lineId(view.lineId())
                .campaignId(view.campaignId())
                .stationName(view.stationName())
                .stampDesignUrl(view.stampDesignUrl())
                .collectedAt(view.collectedAt())
                .collectMethod(view.collectMethod())
                .build();
    }

    public List<UserStampResponse> toUserStampResponses(List<UserStampView> views) {
        return views.stream().map(this::toResponse).toList();
    }

    public PageResponse<UserStampResponse> toUserStampPage(PageResponse<UserStampView> page) {
        return PageResponse.of(
                page.content().stream().map(this::toResponse).toList(),
                page.totalElements(),
                page.totalPages(),
                page.page(),
                page.size()
        );
    }

    public StampBookResponse toResponse(StampBookView view) {
        return StampBookResponse.builder()
                .lineId(view.lineId())
                .lineName(view.lineName())
                .campaignId(view.campaignId())
                .campaignName(view.campaignName())
                .progress(toResponse(view.progress()))
                .stations(view.stations().stream()
                        .map(s -> StampBookResponse.StampBookStationResponse.builder()
                                .stationId(s.stationId())
                                .stationName(s.stationName())
                                .sequence(s.sequence())
                                .collected(s.collected())
                                .stampDesignUrl(s.stampDesignUrl())
                                .build())
                        .toList())
                .build();
    }
}
