package metro.ExoticStamp.modules.user.application.port;

import metro.ExoticStamp.modules.user.application.view.UserView;

import java.util.Optional;
import java.util.UUID;

public interface UserCachePort {
    void put(UUID id, UserView response);
    Optional<UserView> get(UUID id);
    void evict(UUID id);
}
