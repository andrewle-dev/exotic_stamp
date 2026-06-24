package metro.ExoticStamp.modules.metro.application.port;

import metro.ExoticStamp.modules.metro.application.view.StationDetailView;

import java.util.Optional;
import java.util.UUID;

public interface StationCachePort {

    Optional<StationDetailView> getByNfcTagId(String nfcTagId);

    Optional<StationDetailView> getByQrToken(String qrToken);

    Optional<StationDetailView> getByStationId(UUID stationId);

    void putByNfcTagId(String nfcTagId, StationDetailView value);

    void putByQrToken(String qrToken, StationDetailView value);

    void putByStationId(UUID stationId, StationDetailView value);

    void evictByNfcTagId(String nfcTagId);

    void evictByQrToken(String qrToken);

    void evictDetailByStationId(UUID stationId);
}
