package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.modules.metro.application.command.CreateStationCommand;
import metro.ExoticStamp.modules.metro.application.command.ReorderStationsCommand;
import metro.ExoticStamp.modules.metro.application.command.RotateStationQrCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateScanKeysCommand;
import metro.ExoticStamp.modules.metro.application.command.UpdateStationCommand;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.metro.application.support.MetroEnumParser;
import metro.ExoticStamp.common.reorder.ReorderValidation;
import metro.ExoticStamp.modules.metro.application.support.ScanKeyRedactor;
import metro.ExoticStamp.common.reorder.ReorderItemView;
import metro.ExoticStamp.common.reorder.ReorderResultView;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.application.view.StationImageUploadView;
import metro.ExoticStamp.modules.metro.domain.event.StationActivatedEvent;
import metro.ExoticStamp.modules.metro.domain.event.StationDeactivatedEvent;
import metro.ExoticStamp.modules.metro.domain.event.StationQrRotatedEvent;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateNfcTagException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateQrTokenException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationCodeException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationSequenceException;
import metro.ExoticStamp.common.reorder.InvalidReorderException;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidStationStatusException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Line;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationCommandService {

    private final LineRepository lineRepository;
    private final StationRepository stationRepository;
    private final StationCachePort stationCachePort;
    private final MetroAppMapper mapper;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final ApplicationEventPublisher eventPublisher;
    private final MetroAuditHelper metroAuditHelper;

    /** Temporary sort orders above any realistic dense index; clears UNIQUE(line_id, sort_order) collisions. */
    private static final int REORDER_TEMP_BASE = 1_000_000;

    @Transactional
    public StationDetailView createStation(CreateStationCommand command) {
        UUID lineId = command.getLineId();
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        MetroStatus status = MetroEnumParser.parseStatus(command.getStatus());
        if (status == null) {
            status = MetroStatus.DRAFT;
        }
        if (status == MetroStatus.ACTIVE && line.getStatus() != MetroStatus.ACTIVE) {
            throw new InvalidStationStatusException(lineId);
        }
        validateSortOrder(lineId, command.getSortOrder(), null);
        validateNewStationCodes(lineId, command.getCode(), command.getNfcTagId(), command.getQrCodeValue());

        LocalDateTime now = LocalDateTime.now();
        ScanKeyStatus scanKeyStatus = deriveScanKeyStatus(command.getNfcTagId(), command.getQrCodeValue());
        Station station = Station.builder()
                .lineId(lineId)
                .code(command.getCode().trim())
                .name(command.getName().trim())
                .displayName(blankToNull(command.getDisplayName()))
                .sortOrder(command.getSortOrder() != null ? command.getSortOrder() : 0)
                .description(blankToNull(command.getDescription()))
                .address(blankToNull(command.getAddress()))
                .latitude(command.getLatitude())
                .longitude(command.getLongitude())
                .zoneRadiusMeters(command.getZoneRadiusMeters())
                .imageUrl(blankToNull(command.getImageUrl()))
                .stampPreviewUrl(blankToNull(command.getStampPreviewUrl()))
                .nfcTagId(blankToNull(command.getNfcTagId()))
                .qrCodeValue(blankToNull(command.getQrCodeValue()))
                .scanKeyStatus(scanKeyStatus)
                .lastScanKeyUpdatedAt(scanKeyStatus == ScanKeyStatus.ACTIVE ? now : null)
                .collectorCount(0)
                .status(status)
                .createdAt(now)
                .build();
        Station saved = stationRepository.save(station);
        if (status == MetroStatus.ACTIVE) {
            bumpLineTotalStations(lineId, 1);
        }
        metroAuditHelper.scheduleStationCreated(saved.getId().toString());
        return mapper.toStationDetailView(saved, line, true);
    }

    @Transactional
    public StationDetailView updateStation(UpdateStationCommand command) {
        UUID stationId = command.getStationId();
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        Line line = lineRepository.findById(station.getLineId()).orElseThrow(() -> new LineNotFoundException(station.getLineId()));
        MetroStatus wasStatus = station.getStatus();

        if (command.getCode() != null && !command.getCode().isBlank()) {
            String code = command.getCode().trim();
            if (!code.equals(station.getCode())
                    && stationRepository.existsByLineIdAndCodeAndIdNot(station.getLineId(), code, stationId)) {
                throw new DuplicateStationCodeException(code, station.getLineId());
            }
            station.setCode(code);
        }
        if (command.getName() != null && !command.getName().isBlank()) {
            station.setName(command.getName().trim());
        }
        if (command.getDisplayName() != null) {
            station.setDisplayName(blankToNull(command.getDisplayName()));
        }
        if (command.getSortOrder() != null) {
            validateSortOrder(station.getLineId(), command.getSortOrder(), stationId);
            station.setSortOrder(command.getSortOrder());
        }
        if (command.getDescription() != null) {
            station.setDescription(blankToNull(command.getDescription()));
        }
        if (command.getAddress() != null) {
            station.setAddress(blankToNull(command.getAddress()));
        }
        if (command.getLatitude() != null) {
            station.setLatitude(command.getLatitude());
        }
        if (command.getLongitude() != null) {
            station.setLongitude(command.getLongitude());
        }
        if (command.getZoneRadiusMeters() != null) {
            station.setZoneRadiusMeters(command.getZoneRadiusMeters());
        }
        if (command.getImageUrl() != null) {
            station.setImageUrl(blankToNull(command.getImageUrl()));
        }
        if (command.getStampPreviewUrl() != null) {
            station.setStampPreviewUrl(blankToNull(command.getStampPreviewUrl()));
        }
        if (command.getStatus() != null) {
            MetroStatus next = MetroEnumParser.parseStatus(command.getStatus());
            if (next == MetroStatus.ACTIVE && line.getStatus() != MetroStatus.ACTIVE) {
                throw new InvalidStationStatusException(line.getId());
            }
            if (wasStatus != next) {
                if (next == MetroStatus.ACTIVE) {
                    bumpLineTotalStations(station.getLineId(), 1);
                    RbacTransactionCallbacks.afterCommit(
                            () -> eventPublisher.publishEvent(new StationActivatedEvent(station.getId())));
                } else if (wasStatus == MetroStatus.ACTIVE) {
                    bumpLineTotalStations(station.getLineId(), -1);
                    RbacTransactionCallbacks.afterCommit(
                            () -> eventPublisher.publishEvent(new StationDeactivatedEvent(station.getId())));
                }
                station.setStatus(next);
            }
        }

        station.setUpdatedAt(LocalDateTime.now());
        Station saved = stationRepository.save(station);
        evictAllStationCaches(saved);
        metroAuditHelper.scheduleStationUpdated(saved.getId().toString());
        return mapper.toStationDetailView(saved, line, true);
    }

    @Transactional
    public StationDetailView updateScanKeys(UpdateScanKeysCommand command) {
        UUID stationId = command.getStationId();
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        Line line = lineRepository.findById(station.getLineId()).orElseThrow(() -> new LineNotFoundException(station.getLineId()));
        String oldNfc = station.getNfcTagId();
        String oldQr = station.getQrCodeValue();
        LocalDateTime now = LocalDateTime.now();

        if (command.getNfcTagId() != null) {
            String nfc = blankToNull(command.getNfcTagId());
            if (nfc != null && !Objects.equals(nfc, station.getNfcTagId())
                    && stationRepository.existsByNfcTagIdAndIdNot(nfc, stationId)) {
                throw new DuplicateNfcTagException(nfc);
            }
            station.setNfcTagId(nfc);
        }
        if (command.getQrCodeValue() != null) {
            String qr = blankToNull(command.getQrCodeValue());
            if (qr != null && !Objects.equals(qr, station.getQrCodeValue())
                    && stationRepository.existsByQrCodeValueAndIdNot(qr, stationId)) {
                throw new DuplicateQrTokenException(qr);
            }
            station.setQrCodeValue(qr);
        }
        if (command.getScanKeyStatus() != null) {
            station.setScanKeyStatus(MetroEnumParser.parseScanKeyStatus(command.getScanKeyStatus()));
        } else {
            station.setScanKeyStatus(deriveScanKeyStatus(station.getNfcTagId(), station.getQrCodeValue()));
        }
        station.setLastScanKeyUpdatedAt(now);
        station.setUpdatedAt(now);
        Station saved = stationRepository.save(station);
        evictStationCaches(saved, oldNfc, oldQr);
        metroAuditHelper.scheduleScanKeyUpdated(saved.getId().toString(),
                "nfc=" + ScanKeyRedactor.redact(saved.getNfcTagId()) + ",qr=" + ScanKeyRedactor.redact(saved.getQrCodeValue()));
        return mapper.toStationDetailView(saved, line, true);
    }

    @Transactional
    public StationDetailView rotateQr(RotateStationQrCommand command) {
        UUID stationId = command.getStationId();
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        Line line = lineRepository.findById(station.getLineId()).orElseThrow(() -> new LineNotFoundException(station.getLineId()));
        String oldQr = station.getQrCodeValue();
        String newQr = generateUniqueQrValue(stationId);
        station.setQrCodeValue(newQr);
        station.setScanKeyStatus(ScanKeyStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        station.setLastQrRotatedAt(now);
        station.setLastScanKeyUpdatedAt(now);
        station.setUpdatedAt(now);
        Station saved = stationRepository.save(station);
        RbacTransactionCallbacks.afterCommit(() -> {
            if (oldQr != null) {
                stationCachePort.evictByQrToken(oldQr);
            }
            stationCachePort.evictDetailByStationId(saved.getId());
            eventPublisher.publishEvent(new StationQrRotatedEvent(saved.getId(), oldQr, newQr));
        });
        metroAuditHelper.scheduleQrRotated(saved.getId().toString());
        return mapper.toStationDetailView(saved, line, true);
    }

    @Transactional
    public void deleteStation(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (station.getStatus() == MetroStatus.INACTIVE) {
            return;
        }
        if (station.getStatus() == MetroStatus.ACTIVE) {
            bumpLineTotalStations(station.getLineId(), -1);
        }
        station.setStatus(MetroStatus.INACTIVE);
        station.setUpdatedAt(LocalDateTime.now());
        stationRepository.save(station);
        evictAllStationCaches(station);
        RbacTransactionCallbacks.afterCommit(
                () -> eventPublisher.publishEvent(new StationDeactivatedEvent(station.getId())));
        metroAuditHelper.scheduleStationDisabled(stationId.toString());
    }

    /**
     * Dense-renumbers all stations on a line to {@code 0..n-1}.
     * Uses a two-phase update so {@code UNIQUE(line_id, sort_order)} is never violated mid-transaction.
     */
    @Transactional
    public ReorderResultView reorderStations(ReorderStationsCommand command) {
        UUID lineId = command.getLineId();
        if (lineId == null) {
            throw new InvalidReorderException("lineId is required");
        }
        if (lineRepository.findById(lineId).isEmpty()) {
            throw new LineNotFoundException(lineId);
        }

        List<UUID> orderedIds = ReorderValidation.requireOrderedIds(command.getOrderedIds());
        List<Station> scopeStations = stationRepository.findAllByLineId(lineId);
        Set<UUID> scopeIds = scopeStations.stream().map(Station::getId).collect(Collectors.toSet());
        ReorderValidation.requireExactScope(orderedIds, scopeIds, "stations on line " + lineId);

        Map<UUID, Station> byId = new HashMap<>();
        for (Station station : scopeStations) {
            byId.put(station.getId(), station);
        }

        LocalDateTime now = LocalDateTime.now();
        List<Station> ordered = new ArrayList<>(orderedIds.size());
        for (UUID id : orderedIds) {
            ordered.add(byId.get(id));
        }

        // Phase 1: move to temporary unique offsets (still >= 0 for CHECK constraint).
        for (int i = 0; i < ordered.size(); i++) {
            Station station = ordered.get(i);
            station.setSortOrder(REORDER_TEMP_BASE + i);
            station.setUpdatedAt(now);
            stationRepository.save(station);
        }
        stationRepository.flush();

        // Phase 2: dense final order.
        List<ReorderItemView> items = new ArrayList<>(ordered.size());
        for (int i = 0; i < ordered.size(); i++) {
            Station station = ordered.get(i);
            station.setSortOrder(i);
            station.setUpdatedAt(now);
            stationRepository.save(station);
            evictAllStationCaches(station);
            metroAuditHelper.scheduleStationUpdated(station.getId().toString());
            items.add(new ReorderItemView(station.getId(), i));
        }
        return new ReorderResultView(lineId, items.size(), items);
    }

    @Transactional
    public void incrementCollectorCount(UUID stationId) {
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        if (station.getStatus() != MetroStatus.ACTIVE) {
            log.warn("[Metro] reject collectorCount increment for inactive stationId={} principal={}",
                    stationId, currentPrincipalName());
            throw new StationInactiveException(stationId);
        }
        log.info("[Metro] increment collectorCount stationId={} principal={}", stationId, currentPrincipalName());
        int current = station.getCollectorCount() == null ? 0 : station.getCollectorCount();
        final int next;
        try {
            next = Math.addExact(current, 1);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("collectorCount overflow for station: " + stationId);
        }
        station.setCollectorCount(next);
        station.setUpdatedAt(LocalDateTime.now());
        stationRepository.save(station);
        evictAllStationCaches(station);
    }

    @Transactional
    public StationImageUploadView uploadStationImage(UUID stationId, MultipartFile file) {
        if (file == null) {
            throw new InvalidFileException("File is required");
        }
        fileValidator.validate(file);
        Station station = stationRepository.findById(stationId).orElseThrow(() -> new StationNotFoundException(stationId));
        String oldUrl = station.getImageUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            try {
                storageService.delete(oldUrl);
            } catch (Exception e) {
                log.warn("[StationCommand] best-effort delete of old image failed stationId={} err={}", stationId, e.getMessage());
            }
        }
        String folder = "metro/stations/" + stationId;
        String url = storageService.upload(file, folder);
        station.setImageUrl(url);
        station.setUpdatedAt(LocalDateTime.now());
        stationRepository.save(station);
        evictAllStationCaches(station);
        return new StationImageUploadView(url);
    }

    private String generateUniqueQrValue(UUID stationId) {
        for (int i = 0; i < 5; i++) {
            String candidate = "QR-" + UUID.randomUUID();
            if (!stationRepository.existsByQrCodeValueAndIdNot(candidate, stationId)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique QR value for station: " + stationId);
    }

    private void validateSortOrder(UUID lineId, Integer sortOrder, UUID stationId) {
        if (sortOrder == null) {
            return;
        }
        boolean exists = stationId == null
                ? stationRepository.existsByLineIdAndSortOrder(lineId, sortOrder)
                : stationRepository.existsByLineIdAndSortOrderAndIdNot(lineId, sortOrder, stationId);
        if (exists) {
            throw new DuplicateStationSequenceException(lineId, sortOrder);
        }
    }

    private void validateNewStationCodes(UUID lineId, String code, String nfcTagId, String qrCodeValue) {
        if (stationRepository.existsByLineIdAndCode(lineId, code)) {
            throw new DuplicateStationCodeException(code, lineId);
        }
        String nfc = blankToNull(nfcTagId);
        if (nfc != null && stationRepository.existsByNfcTagId(nfc)) {
            throw new DuplicateNfcTagException(nfc);
        }
        String qr = blankToNull(qrCodeValue);
        if (qr != null && stationRepository.existsByQrCodeValue(qr)) {
            throw new DuplicateQrTokenException(qr);
        }
    }

    private static ScanKeyStatus deriveScanKeyStatus(String nfcTagId, String qrCodeValue) {
        return blankToNull(nfcTagId) != null || blankToNull(qrCodeValue) != null
                ? ScanKeyStatus.ACTIVE : ScanKeyStatus.INACTIVE;
    }

    private void bumpLineTotalStations(UUID lineId, int delta) {
        Line line = lineRepository.findById(lineId).orElseThrow(() -> new LineNotFoundException(lineId));
        int current = line.getTotalStations() == null ? 0 : line.getTotalStations();
        line.setTotalStations(Math.max(0, current + delta));
        line.setUpdatedAt(LocalDateTime.now());
        lineRepository.save(line);
    }

    private void evictAllStationCaches(Station station) {
        stationCachePort.evictDetailByStationId(station.getId());
        if (station.getNfcTagId() != null) {
            stationCachePort.evictByNfcTagId(station.getNfcTagId());
        }
        if (station.getQrCodeValue() != null) {
            stationCachePort.evictByQrToken(station.getQrCodeValue());
        }
    }

    private void evictStationCaches(Station station, String oldNfc, String oldQr) {
        stationCachePort.evictDetailByStationId(station.getId());
        if (oldNfc != null && !oldNfc.equals(station.getNfcTagId())) {
            stationCachePort.evictByNfcTagId(oldNfc);
        }
        if (oldQr != null && !oldQr.equals(station.getQrCodeValue())) {
            stationCachePort.evictByQrToken(oldQr);
        }
        if (station.getNfcTagId() != null) {
            stationCachePort.evictByNfcTagId(station.getNfcTagId());
        }
        if (station.getQrCodeValue() != null) {
            stationCachePort.evictByQrToken(station.getQrCodeValue());
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String currentPrincipalName() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            return auth == null ? "anonymous" : auth.getName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
