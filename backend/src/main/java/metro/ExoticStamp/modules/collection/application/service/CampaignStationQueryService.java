package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.view.CampaignStationView;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignStationQueryService {

    private final CampaignRepository campaignRepository;
    private final CampaignStationRepository campaignStationRepository;
    private final StationReadPort stationReadPort;

    public List<CampaignStationView> listByCampaignId(UUID campaignId) {
        if (campaignRepository.findByIdNotDeleted(campaignId).isEmpty()) {
            throw new CampaignNotFoundException(campaignId);
        }
        List<UUID> stationIds = campaignStationRepository.findStationIdsByCampaignId(campaignId);
        if (stationIds.isEmpty()) {
            return List.of();
        }
        Map<UUID, MetroStationView> byId = stationReadPort.listStationViewsByIds(stationIds).stream()
                .collect(Collectors.toMap(MetroStationView::id, Function.identity()));
        List<CampaignStationView> result = new ArrayList<>();
        for (UUID stationId : stationIds) {
            MetroStationView station = byId.get(stationId);
            if (station != null) {
                result.add(CampaignStationView.builder()
                        .stationId(station.id())
                        .name(station.name())
                        .displayName(station.name())
                        .lineId(station.lineId())
                        .sortOrder(station.sequence() != null ? station.sequence() : 0)
                        .build());
            }
        }
        return result;
    }
}
