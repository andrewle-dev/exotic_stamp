package metro.ExoticStamp.modules.metro.domain.repository;

import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StationRepository {

    Optional<Station> findById(UUID id);

    List<Station> findAllByIdIn(Collection<UUID> ids);

    Station save(Station station);

    /** Flushes pending persistence work (needed for two-phase unique-safe renumber). */
    void flush();

    List<Station> findAllByLineId(UUID lineId);

    List<Station> findAllByLineIdAndStatus(UUID lineId, MetroStatus status);

    List<Station> findAllStationsOrdered();

    List<Station> findAllByStatus(MetroStatus status);

    List<Station> findAllActiveUnderActiveLines();

    Optional<Station> findByNfcTagId(String nfcTagId);

    Optional<Station> findByQrCodeValue(String qrCodeValue);

    boolean existsByLineIdAndCode(UUID lineId, String code);

    boolean existsByLineIdAndCodeAndIdNot(UUID lineId, String code, UUID id);

    boolean existsByNfcTagId(String nfcTagId);

    boolean existsByQrCodeValue(String qrCodeValue);

    boolean existsByNfcTagIdAndIdNot(String nfcTagId, UUID id);

    boolean existsByQrCodeValueAndIdNot(String qrCodeValue, UUID id);

    boolean existsByLineIdAndSortOrder(UUID lineId, Integer sortOrder);

    boolean existsByLineIdAndSortOrderAndIdNot(UUID lineId, Integer sortOrder, UUID id);

    List<Object[]> findTop20StationStatsRaw();

    MetroPageResult<Station> search(UUID lineId, MetroStatus status, String search, int page, int size, String sortField, boolean ascending);
}
