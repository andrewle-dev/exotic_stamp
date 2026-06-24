package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.command.ScanResolveCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import metro.ExoticStamp.modules.metro.domain.exception.LineInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.application.support.MetroEnumParser;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetroScanResolveService {

    private final StationRepository stationRepository;
    private final LineRepository lineRepository;
    private final MetroAppMapper mapper;

    public ScanResolveView resolve(ScanResolveCommand command) {
        ScanType scanType = MetroEnumParser.parseScanType(command.getScanType());
        if (scanType == null) {
            throw new InvalidScanPayloadException("scanType is required");
        }
        String payload = normalizePayload(command.getPayload());
        Station station = lookupStation(scanType, payload);
        Line line = lineRepository.findById(station.getLineId())
                .orElseThrow(ScanKeyNotFoundException::new);

        if (station.getScanKeyStatus() != ScanKeyStatus.ACTIVE) {
            throw new ScanKeyInactiveException();
        }
        if (station.getStatus() != MetroStatus.ACTIVE) {
            throw new StationInactiveException(station.getId());
        }
        if (line.getStatus() != MetroStatus.ACTIVE) {
            throw new LineInactiveException(line.getId());
        }
        return mapper.toScanResolveView(station, line, scanType);
    }

    private Station lookupStation(ScanType scanType, String payload) {
        return switch (scanType) {
            case NFC -> stationRepository.findByNfcTagId(payload).orElseThrow(ScanKeyNotFoundException::new);
            case QR_STATIC, QR_DYNAMIC_PLACEHOLDER ->
                    stationRepository.findByQrCodeValue(payload).orElseThrow(ScanKeyNotFoundException::new);
        };
    }

    private static String normalizePayload(String payload) {
        if (payload == null) {
            throw new InvalidScanPayloadException("payload is required");
        }
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidScanPayloadException("payload must not be blank");
        }
        return trimmed;
    }
}
