package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.exceptions.storage.ConcurrentAssetReplaceException;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.asset.AssetLifecycleService;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Separate bean so {@code @Transactional} applies after S3/local PutObject succeeds.
 */
@Service
@RequiredArgsConstructor
public class StationImagePointerService {

    private final StationRepository stationRepository;
    private final StationCachePort stationCachePort;
    private final AssetLifecycleService assetLifecycleService;

    @Transactional
    public void applyPointer(
            UUID stationId,
            String expectedOldUrl,
            StorageUploadResult result,
            UUID pendingAssetId
    ) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new StationNotFoundException(stationId));
        String current = station.getImageUrl();
        boolean unchanged = (expectedOldUrl == null || expectedOldUrl.isBlank())
                ? (current == null || current.isBlank())
                : Objects.equals(expectedOldUrl, current);
        if (!unchanged) {
            throw new ConcurrentAssetReplaceException(
                    "Station image was replaced by another request; retry with the latest image");
        }
        station.setImageUrl(result.publicUrl());
        station.setUpdatedAt(LocalDateTime.now());
        stationRepository.save(station);
        assetLifecycleService.activate(pendingAssetId);
        assetLifecycleService.orphanPrevious(expectedOldUrl, null);
        stationCachePort.evictDetailByStationId(stationId);
        if (station.getNfcTagId() != null) {
            stationCachePort.evictByNfcTagId(station.getNfcTagId());
        }
        if (station.getQrCodeValue() != null) {
            stationCachePort.evictByQrToken(station.getQrCodeValue());
        }
    }
}
