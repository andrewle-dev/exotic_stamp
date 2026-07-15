package metro.ExoticStamp.modules.collection.presentation.mapper;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.view.CollectStampResultView;
import metro.ExoticStamp.modules.collection.application.view.CollectStatusView;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;
import metro.ExoticStamp.modules.collection.presentation.response.CollectStampResponse;
import metro.ExoticStamp.modules.collection.presentation.response.CollectStatusResponse;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampBookResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
import org.springframework.stereotype.Component;

@Component
public class CollectionRuntimePresentationMapper {

    public CollectStampResponse toResponse(CollectStampResultView view) {
        return CollectStampResponse.builder()
                .stamp(CollectStampResponse.StampInfoResponse.builder()
                        .stampId(view.stamp().stampId())
                        .stationId(view.stamp().stationId())
                        .stationName(view.stamp().stationName())
                        .lineName(view.stamp().lineName())
                        .lineId(view.stamp().lineId())
                        .campaignId(view.stamp().campaignId())
                        .stampDesignUrl(view.stamp().stampDesignUrl())
                        .collectedAt(view.stamp().collectedAt())
                        .build())
                .progress(toProgress(view.progress()))
                .scan(CollectStampResponse.ScanInfoResponse.builder()
                        .scanType(view.scan().scanType())
                        .gpsDistanceMeters(view.scan().gpsDistanceMeters())
                        .gpsAccuracyMeters(view.scan().gpsAccuracyMeters())
                        .build())
                .isNew(view.isNew())
                .build();
    }

    public CollectStatusResponse toStatusResponse(CollectStatusView view) {
        if (view == null) {
            return null;
        }
        CollectStampResultView.StampInfo stamp = view.stamp();
        CollectStampResultView.ScanInfo scan = view.scan();
        return CollectStatusResponse.builder()
                .status(view.status())
                .stamp(stamp == null ? null : CollectStampResponse.StampInfoResponse.builder()
                        .stampId(stamp.stampId())
                        .stationId(stamp.stationId())
                        .stationName(stamp.stationName())
                        .lineName(stamp.lineName())
                        .lineId(stamp.lineId())
                        .campaignId(stamp.campaignId())
                        .stampDesignUrl(stamp.stampDesignUrl())
                        .collectedAt(stamp.collectedAt())
                        .build())
                .scan(scan == null ? null : CollectStampResponse.ScanInfoResponse.builder()
                        .scanType(scan.scanType())
                        .gpsDistanceMeters(scan.gpsDistanceMeters())
                        .gpsAccuracyMeters(scan.gpsAccuracyMeters())
                        .build())
                .progress(toProgress(view.progress()))
                .createdAt(view.createdAt())
                .resolvedAt(view.resolvedAt())
                .errorCode(view.errorCode())
                .build();
    }

    public StampBookResponse toResponse(StampBookView view) {
        return StampBookResponse.builder()
                .lineId(view.lineId())
                .lineName(view.lineName())
                .campaignId(view.campaignId())
                .campaignName(view.campaignName())
                .progress(toProgress(view.progress()))
                .stations(view.stations().stream()
                        .map(s -> StampBookResponse.StampBookStationResponse.builder()
                                .stationId(s.stationId())
                                .stationName(s.stationName())
                                .sequence(s.sequence())
                                .collected(s.collected())
                                .stampDesignUrl(s.stampDesignUrl())
                                .stampDesignName(s.stampDesignName())
                                .stampDesignDescription(s.stampDesignDescription())
                                .rarity(s.rarity())
                                .collectedAt(s.collectedAt())
                                .build())
                        .toList())
                .build();
    }

    public PageResponse<UserStampResponse> toUserStampPage(PageResponse<UserStampView> page) {
        return PageResponse.of(
                page.content().stream().map(this::toUserStamp).toList(),
                page.totalElements(),
                page.totalPages(),
                page.page(),
                page.size()
        );
    }

    private UserStampResponse toUserStamp(UserStampView view) {
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

    private ProgressResponse toProgress(ProgressView view) {
        if (view == null) {
            return null;
        }
        return ProgressResponse.builder()
                .lineId(view.lineId())
                .collected(view.collected())
                .total(view.total())
                .percentage(view.percentage())
                .build();
    }

    public ProgressResponse toProgressResponse(ProgressView view) {
        return toProgress(view);
    }
}
