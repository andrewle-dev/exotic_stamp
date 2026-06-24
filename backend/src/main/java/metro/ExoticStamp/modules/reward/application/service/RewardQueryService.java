package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @deprecated Use {@link UserRewardQueryService}.
 */
@Deprecated
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RewardQueryService {

    private final UserRewardQueryService userRewardQueryService;

    public PageResponse<UserRewardView> getMyRewards(UUID userId, String status, int page, int size) {
        return userRewardQueryService.listMyRewards(userId, status, page, size);
    }

    public UserRewardView getMyRewardDetail(UUID userId, UUID userRewardId) {
        return userRewardQueryService.getMyReward(userId, userRewardId);
    }
}
