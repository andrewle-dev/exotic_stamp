package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.view.ActiveCampaignStationView;
import metro.ExoticStamp.modules.collection.application.view.ActiveCampaignView;
import metro.ExoticStamp.modules.collection.application.view.StampPreviewView;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActiveCampaignQueryService {

    private final CampaignRepository campaignRepository;
    private final CampaignStationRepository campaignStationRepository;
    private final StampDesignRepository stampDesignRepository;
    private final StationReadPort stationReadPort;
    private final LineReadPort lineReadPort;
    private final CampaignAppMapper campaignAppMapper;
    private final Clock clock;

    public List<ActiveCampaignView> listActive() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Campaign> campaigns = campaignRepository.findActiveInWindow(now);
        List<ActiveCampaignView> result = new ArrayList<>();
        for (Campaign campaign : campaigns) {
            result.add(buildActiveView(campaign, now));
        }
        return result;
    }

    public ActiveCampaignView getActiveById(UUID id) {
        LocalDateTime now = LocalDateTime.now(clock);
        Campaign campaign = campaignRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
        if (!isPubliclyActive(campaign, now)) {
            throw new CampaignNotFoundException(id);
        }
        return buildActiveView(campaign, now);
    }

    public List<ActiveCampaignStationView> listActiveStations(UUID campaignId) {
        ActiveCampaignView view = getActiveById(campaignId);
        return view.stations();
    }

    public List<ActiveCampaignView> listActiveByStationId(UUID stationId) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<Campaign> campaigns = campaignRepository.findActiveByStationId(stationId, now);
        List<ActiveCampaignView> result = new ArrayList<>();
        for (Campaign campaign : campaigns) {
            result.add(buildActiveView(campaign, now));
        }
        return result;
    }

    private ActiveCampaignView buildActiveView(Campaign campaign, LocalDateTime now) {
        List<UUID> stationIds = campaignStationRepository.findStationIdsByCampaignId(campaign.getId());
        List<MetroStationView> stations = stationReadPort.listStationViewsByIds(stationIds);
        Map<UUID, StampDesign> designsByStation = stampDesignRepository
                .findActiveByCampaignIdAndStationIdIn(campaign.getId(), stationIds).stream()
                .collect(Collectors.toMap(StampDesign::getStationId, Function.identity(), (a, b) -> a));

        List<ActiveCampaignStationView> stationViews = new ArrayList<>();
        for (MetroStationView station : stations) {
            if (!station.active()) {
                continue;
            }
            var line = lineReadPort.getLineById(station.lineId());
            if (!line.active()) {
                continue;
            }
            StampDesign design = designsByStation.get(station.id());
            StampPreviewView preview = campaignAppMapper.toStampPreviewView(design);
            stationViews.add(ActiveCampaignStationView.builder()
                    .id(station.id())
                    .name(station.name())
                    .displayName(station.name())
                    .sortOrder(station.sequence() != null ? station.sequence() : 0)
                    .stampPreview(preview)
                    .build());
        }
        stationViews.sort((a, b) -> Integer.compare(a.sortOrder(), b.sortOrder()));

        return ActiveCampaignView.builder()
                .id(campaign.getId())
                .code(campaign.getCode())
                .name(campaign.getName())
                .displayName(campaign.getDisplayName())
                .description(campaign.getDescription())
                .campaignType(campaign.getCampaignType().name())
                .bannerImageUrl(campaign.getBannerImageUrl())
                .thumbnailImageUrl(campaign.getThumbnailImageUrl())
                .priority(campaign.getPriority())
                .startAt(campaign.getStartAt())
                .endAt(campaign.getEndAt())
                .stations(stationViews)
                .build();
    }

    private boolean isPubliclyActive(Campaign campaign, LocalDateTime now) {
        return campaign.getStatus() == CampaignStatus.ACTIVE
                && !campaign.isDeleted()
                && !now.isBefore(campaign.getStartAt())
                && !now.isAfter(campaign.getEndAt());
    }
}
