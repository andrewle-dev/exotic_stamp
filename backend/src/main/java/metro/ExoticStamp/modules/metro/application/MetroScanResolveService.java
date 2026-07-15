package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.command.ScanResolveCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.support.MetroEnumParser;
import metro.ExoticStamp.modules.metro.application.support.ScanKeyHasher;
import metro.ExoticStamp.modules.metro.application.support.ScanPayloadParser;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import metro.ExoticStamp.modules.metro.domain.exception.LineInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationScanKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetroScanResolveService {

    private final StationRepository stationRepository;
    private final LineRepository lineRepository;
    private final StationScanKeyRepository stationScanKeyRepository;
    private final StationScanKeyCommandService stationScanKeyCommandService;
    private final ScanPayloadParser scanPayloadParser;
    private final ScanKeyHasher scanKeyHasher;
    private final MetroAppMapper mapper;

    public ScanResolveView resolve(ScanResolveCommand command) {
        ScanType scanType = MetroEnumParser.parseScanType(command.getScanType());
        if (scanType == null) {
            throw new InvalidScanPayloadException("scanType is required");
        }

        String rawKey = scanPayloadParser.extractRawKey(command.getPayload());
        String keyHash = scanKeyHasher.hash(rawKey);

        Optional<StationScanKey> byHash = stationScanKeyRepository.findByKeyHash(keyHash);
        if (byHash.isPresent()) {
            StationScanKey scanKey = byHash.get();
            if (scanKey.getScanType() != scanType) {
                throw new ScanKeyNotFoundException();
            }
            if (scanKey.getStatus() != ScanKeyStatus.ACTIVE) {
                throw new ScanKeyInactiveException();
            }
            Station station = stationRepository.findById(scanKey.getStationId())
                    .orElseThrow(ScanKeyNotFoundException::new);
            Line line = requireActiveLine(station);
            requireActiveStation(station);
            stationScanKeyCommandService.recordLastSeen(scanKey.getId());
            return mapper.toScanResolveView(station, line, scanType);
        }

        Station station = lookupLegacyStation(scanType, rawKey);
        Line line = requireActiveLine(station);
        if (station.getScanKeyStatus() != ScanKeyStatus.ACTIVE) {
            throw new ScanKeyInactiveException();
        }
        requireActiveStation(station);
        return mapper.toScanResolveView(station, line, scanType);
    }

    private Station lookupLegacyStation(ScanType scanType, String payload) {
        return switch (scanType) {
            case NFC -> stationRepository.findByNfcTagId(payload).orElseThrow(ScanKeyNotFoundException::new);
            case QR_STATIC, QR_DYNAMIC_PLACEHOLDER ->
                    stationRepository.findByQrCodeValue(payload).orElseThrow(ScanKeyNotFoundException::new);
        };
    }

    private Line requireActiveLine(Station station) {
        Line line = lineRepository.findById(station.getLineId())
                .orElseThrow(ScanKeyNotFoundException::new);
        if (line.getStatus() != MetroStatus.ACTIVE) {
            throw new LineInactiveException(line.getId());
        }
        return line;
    }

    private static void requireActiveStation(Station station) {
        if (station.getStatus() != MetroStatus.ACTIVE) {
            throw new StationInactiveException(station.getId());
        }
    }
}
