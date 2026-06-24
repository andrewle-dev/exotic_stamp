package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.support.MetroEnumParser;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.view.LineDetailView;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.application.view.MetroPageSlice;
import metro.ExoticStamp.modules.metro.application.view.StationView;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.MetroPageResult;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LineQueryService implements LineReadPort {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final MetroAppMapper mapper;

    public List<LineView> getPublicLines() {
        return lineRepository.findAllByStatus(MetroStatus.ACTIVE).stream()
                .map(mapper::toLineView)
                .toList();
    }

    public LineDetailView getPublicLineDetail(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        if (line.getStatus() != MetroStatus.ACTIVE) {
            throw new LineNotFoundException(lineId);
        }
        return buildLineDetail(line, true);
    }

    public MetroPageSlice<LineView> searchAdminLines(String status, String search, int page, int size, String sort) {
        SortParams sp = parseSort(sort);
        MetroStatus metroStatus = MetroEnumParser.parseStatus(status);
        MetroPageResult<Line> result = lineRepository.search(metroStatus, search, page, size, sp.field(), sp.ascending());
        List<LineView> views = result.content().stream().map(mapper::toLineView).toList();
        return new MetroPageSlice<>(views, result.totalElements(), result.totalPages(), result.page(), result.size());
    }

    public LineView getAdminLine(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        return mapper.toLineView(line);
    }

    public LineDetailView getAdminLineDetail(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        return buildLineDetail(line, false);
    }

    @Override
    public List<MetroLineView> getAllActiveLines() {
        return lineRepository.findAllByStatus(MetroStatus.ACTIVE).stream().map(this::toSharedLineView).toList();
    }

    @Override
    public MetroLineView getLineById(UUID lineId) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        return toSharedLineView(line);
    }

    private LineDetailView buildLineDetail(Line line, boolean activeStationsOnly) {
        List<Station> stations = activeStationsOnly
                ? stationRepository.findAllByLineIdAndStatus(line.getId(), MetroStatus.ACTIVE)
                : stationRepository.findAllByLineId(line.getId());
        Map<UUID, Line> lineMap = Map.of(line.getId(), line);
        List<StationView> summaries = stations.stream()
                .map(s -> mapper.toStationView(s, lineMap.get(s.getLineId())))
                .toList();
        return mapper.toLineDetailView(line, summaries);
    }

    private MetroLineView toSharedLineView(Line line) {
        return MetroLineView.builder()
                .id(line.getId())
                .code(line.getCode())
                .name(line.getName())
                .active(line.getStatus() == MetroStatus.ACTIVE)
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
