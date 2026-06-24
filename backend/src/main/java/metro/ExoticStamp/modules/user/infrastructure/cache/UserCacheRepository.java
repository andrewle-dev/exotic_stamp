package metro.ExoticStamp.modules.user.infrastructure.cache;

import metro.ExoticStamp.config.CacheProperties;
import metro.ExoticStamp.infra.cache.BaseCacheRepository;
import metro.ExoticStamp.modules.user.application.port.UserCachePort;
import metro.ExoticStamp.modules.user.application.view.UserView;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class UserCacheRepository extends BaseCacheRepository<UserView> implements UserCachePort {

    private final CacheProperties cacheProperties;

    protected UserCacheRepository(RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry, CacheProperties cacheProperties) {
        super(redisTemplate, meterRegistry);
        this.cacheProperties = cacheProperties;
    }

    @Override protected String prefix()        { return "user:"; }
    @Override protected Duration ttl()         { return cacheProperties.getUserTtl(); }
    @Override protected Class<UserView> type() { return UserView.class; }
}
