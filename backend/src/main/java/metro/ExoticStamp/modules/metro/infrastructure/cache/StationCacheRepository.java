package metro.ExoticStamp.modules.metro.infrastructure.cache;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Repository
public class StationCacheRepository extends BaseCacheRepository<StationDetailView> implements StationCachePort {

    private static final String NFC_SUFFIX = "nfc:";
    private static final String QR_SUFFIX = "qr:";
    private static final String DETAIL_SUFFIX = "detail:";

    private final Duration scanTtl;
    private final Duration detailTtl;

    public StationCacheRepository(
            RedisTemplate<String, Object> redisTemplate,
            MeterRegistry meterRegistry,
            CacheProperties cacheProperties
    ) {
        super(redisTemplate, meterRegistry);
        this.scanTtl = cacheProperties.getTtl().getMetroStationScan();
        this.detailTtl = cacheProperties.getTtl().getStationDetail();
    }

    @Override
    protected String prefix() {
        return "station:";
    }

    @Override
    protected Duration ttl() {
        return scanTtl;
    }

    @Override
    protected Class<StationDetailView> type() {
        return StationDetailView.class;
    }

    @Override
    public Optional<StationDetailView> getByNfcTagId(String nfcTagId) {
        return getString(prefix() + NFC_SUFFIX + nfcTagId);
    }

    @Override
    public Optional<StationDetailView> getByQrToken(String qrToken) {
        return getString(prefix() + QR_SUFFIX + qrToken);
    }

    @Override
    public Optional<StationDetailView> getByStationId(UUID stationId) {
        return getString(prefix() + DETAIL_SUFFIX + stationId);
    }

    @Override
    public void putByNfcTagId(String nfcTagId, StationDetailView value) {
        putString(prefix() + NFC_SUFFIX + nfcTagId, value, scanTtl);
    }

    @Override
    public void putByQrToken(String qrToken, StationDetailView value) {
        putString(prefix() + QR_SUFFIX + qrToken, value, scanTtl);
    }

    @Override
    public void putByStationId(UUID stationId, StationDetailView value) {
        putString(prefix() + DETAIL_SUFFIX + stationId, value, detailTtl);
    }

    @Override
    public void evictByNfcTagId(String nfcTagId) {
        if (nfcTagId != null && !nfcTagId.isBlank()) {
            deleteKey(prefix() + NFC_SUFFIX + nfcTagId);
        }
    }

    @Override
    public void evictByQrToken(String qrToken) {
        if (qrToken != null && !qrToken.isBlank()) {
            deleteKey(prefix() + QR_SUFFIX + qrToken);
        }
    }

    @Override
    public void evictDetailByStationId(UUID stationId) {
        if (stationId != null) {
            deleteKey(prefix() + DETAIL_SUFFIX + stationId);
        }
    }

    private Optional<StationDetailView> getString(String key) {
        try {
            Object raw = redisTemplate.opsForValue().get(key);
            if (raw == null) {
                meterRegistry.counter("cache.miss", "domain", "station").increment();
                return Optional.empty();
            }
            meterRegistry.counter("cache.hit", "domain", "station").increment();
            return Optional.of(type().cast(raw));
        } catch (Exception e) {
            meterRegistry.counter("cache.error", "domain", "station").increment();
            log.warn("[StationCache] get failed key={}: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    private void putString(String key, StationDetailView value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("[StationCache] put failed key={}: {}", key, e.getMessage());
        }
    }

    private void deleteKey(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("[StationCache] delete failed key={}: {}", key, e.getMessage());
        }
    }
}
