package metro.ExoticStamp.modules.reward.application.port;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;

import java.util.Optional;
import java.util.UUID;

public interface RewardCachePort {

    Optional<UserRewardView> getUserRewardDetail(UUID userId, UUID userRewardId);

    void putUserRewardDetail(UUID userId, UUID userRewardId, UserRewardView view);

    void evictUserRewardDetail(UUID userId, UUID userRewardId);

    Optional<PageResponse<UserRewardView>> getUserRewardList(UUID userId, int page, int size);

    void putUserRewardList(UUID userId, int page, int size, PageResponse<UserRewardView> pageResponse);

    void evictUserRewardListAll(UUID userId);
}
