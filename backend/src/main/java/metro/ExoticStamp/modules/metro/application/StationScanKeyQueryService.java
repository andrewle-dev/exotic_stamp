package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.mapper.StationScanKeyAppMapper;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyView;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationScanKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StationScanKeyQueryService {

    private final StationScanKeyRepository stationScanKeyRepository;
    private final StationRepository stationRepository;
    private final StationScanKeyAppMapper mapper;

    public List<StationScanKeyView> listByStationId(UUID stationId) {
        if (!stationRepository.findById(stationId).isPresent()) {
            throw new StationNotFoundException(stationId);
        }
        return stationScanKeyRepository.findAllByStationIdOrderByCreatedAtDesc(stationId).stream()
                .map(mapper::toView)
                .toList();
    }

    public StationScanKeyView getById(UUID id) {
        return stationScanKeyRepository.findById(id)
                .map(mapper::toView)
                .orElseThrow(ScanKeyNotFoundException::new);
    }
}
