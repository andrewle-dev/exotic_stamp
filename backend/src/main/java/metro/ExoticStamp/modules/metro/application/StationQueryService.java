package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroPageSlice;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationStatsView;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.application.support.MetroEnumParser;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.MetroPageResult;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationQueryService implements StationReadPort {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final MetroAppMapper mapper;

    public List<StationView> getPublicStations(UUID lineId) {
        List<Station> stations = lineId != null
                ? loadPublicStationsForLine(lineId)
                : stationRepository.findAllActiveUnderActiveLines();
        return toStationViews(stations);
    }

    public List<StationView> getPublicStationsByLine(UUID lineId) {
        return toStationViews(loadPublicStationsForLine(lineId));
    }

    public StationDetailView getPublicStationDetail(UUID stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId));
        Line line = lineRepository.findById(station.getLineId())
                .orElseThrow(() -> new LineNotFoundException(station.getLineId()));
        if (!MetroAppMapper.isPubliclyVisible(station, line)) {
            throw new StationNotFoundException(stationId);
        }
        return mapper.toStationDetailView(station, line, false);
    }

    public MetroPageSlice<StationView> searchAdminStations(UUID lineId, String status, String search,
                                                           int page, int size, String sort) {
        SortParams sp = parseSort(sort);
        MetroStatus metroStatus = MetroEnumParser.parseStatus(status);
        MetroPageResult<Station> result = stationRepository.search(lineId, metroStatus, search, page, size, sp.field(), sp.ascending());
        List<StationView> views = toStationViews(result.content());
        return new MetroPageSlice<>(views, result.totalElements(), result.totalPages(), result.page(), result.size());
    }

    public StationDetailView getAdminStationDetail(UUID stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId));
        Line line = lineRepository.findById(station.getLineId())
                .orElseThrow(() -> new LineNotFoundException(station.getLineId()));
        return mapper.toStationDetailView(station, line, true);
    }

    public List<StationStatsView> stationStats() {
        return stationRepository.findTop20StationStatsRaw().stream()
                .map(row -> StationStatsView.builder()
                        .stationId((UUID) row[0])
                        .stationName((String) row[1])
                        .lineName((String) row[2])
                        .collectorCount(((Number) row[3]).intValue())
                        .build())
                .toList();
    }

    @Override
    public MetroStationView resolveStationViewByNfc(String nfcTagId) {
        Station station = stationRepository.findByNfcTagId(nfcTagId)
                .orElseThrow(() -> new StationNotFoundException("nfcTagId", nfcTagId));
        if (station.getStatus() != MetroStatus.ACTIVE) {
            throw new StationInactiveException(station.getId());
        }
        return toSharedStationView(station);
    }

    @Override
    public MetroStationView resolveStationViewByQr(String qrToken) {
        Station station = stationRepository.findByQrCodeValue(qrToken)
                .orElseThrow(() -> new StationNotFoundException("qrCodeValue", qrToken));
        if (station.getStatus() != MetroStatus.ACTIVE) {
            throw new StationInactiveException(station.getId());
        }
        return toSharedStationView(station);
    }

    @Override
    public MetroStationView getStationViewById(UUID stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId));
        return toSharedStationView(station);
    }

    @Override
    public List<MetroStationView> listActiveStationsByLineId(UUID lineId) {
        lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        return stationRepository.findAllByLineIdAndStatus(lineId, MetroStatus.ACTIVE).stream()
                .map(this::toSharedStationView)
                .toList();
    }

    @Override
    public List<MetroStationView> listStationViewsByIds(Collection<UUID> stationIds) {
        if (stationIds == null || stationIds.isEmpty()) {
            return List.of();
        }
        return stationRepository.findAllByIdIn(stationIds.stream().distinct().toList()).stream()
                .map(this::toSharedStationView)
                .toList();
    }

    private List<Station> loadPublicStationsForLine(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        if (line.getStatus() != MetroStatus.ACTIVE) {
            throw new LineNotFoundException(lineId);
        }
        return stationRepository.findAllByLineIdAndStatus(lineId, MetroStatus.ACTIVE);
    }

    private List<StationView> toStationViews(List<Station> stations) {
        if (stations.isEmpty()) {
            return List.of();
        }
        List<UUID> lineIds = stations.stream().map(Station::getLineId).distinct().toList();
        Map<UUID, Line> lineMap = lineRepository.findAllByIdIn(lineIds).stream()
                .collect(Collectors.toMap(Line::getId, Function.identity()));
        return stations.stream()
                .map(s -> mapper.toStationView(s, lineMap.get(s.getLineId())))
                .toList();
    }

    private MetroStationView toSharedStationView(Station station) {
        return MetroStationView.builder()
                .id(station.getId())
                .lineId(station.getLineId())
                .name(station.getName())
                .sequence(station.getSortOrder())
                .active(station.getStatus() == MetroStatus.ACTIVE)
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .build();
    }

    private static SortParams parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return new SortParams("sortOrder", true);
        }
        String[] parts = sort.split(",");
        String field = parts[0].trim();
        boolean asc = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].trim());
        return new SortParams(field, asc);
    }

    private record SortParams(String field, boolean ascending) {
    }
}
