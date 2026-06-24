package metro.ExoticStamp.modules.collection.application.port;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.StampBookView;
import metro.ExoticStamp.modules.collection.application.view.UserStampView;

import java.util.Optional;
import java.util.UUID;

public interface UserStampCachePort {

    Optional<PageResponse<UserStampView>> getUserStamps(UUID userId, UUID lineId, UUID campaignId, int page, int size);

    void putUserStamps(UUID userId, UUID lineId, UUID campaignId, int page, int size, PageResponse<UserStampView> value);

    void evictUserStampsForLine(UUID userId, UUID lineId);

    Optional<ProgressView> getUserProgress(UUID userId, UUID lineId);

    void putUserProgress(UUID userId, UUID lineId, ProgressView value);

    void evictUserProgress(UUID userId, UUID lineId);

    Optional<PageResponse<UserStampView>> getUserHistory(UUID userId, int page, int size);

    void putUserHistory(UUID userId, int page, int size, PageResponse<UserStampView> value);

    void evictUserHistoryAll(UUID userId);

    Optional<StampBookView> getStampBook(UUID userId, UUID lineId, UUID campaignId);

    void putStampBook(UUID userId, UUID lineId, UUID campaignId, StampBookView value);

    void evictStampBook(UUID userId, UUID lineId, UUID campaignId);

    void evictAllForUserCollection(UUID userId, UUID lineId, UUID campaignId);
}
