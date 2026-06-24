package metro.ExoticStamp.modules.community.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.view.MyReferralsView;
import metro.ExoticStamp.modules.community.application.view.ReferralView;
import metro.ExoticStamp.modules.community.domain.model.Referral;
import metro.ExoticStamp.modules.community.domain.model.ReferralStatus;
import metro.ExoticStamp.modules.community.domain.repository.ReferralRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReferralQueryService {

    private final ReferralRepository referralRepository;
    private final CommunityAppMapper communityAppMapper;

    public MyReferralsView getMyReferrals(UUID userId) {
        ReferralView referredBy = referralRepository.findByReferredUserId(userId)
                .map(communityAppMapper::toReferralView)
                .orElse(null);

        List<ReferralView> referredUsers = referralRepository.findByReferrerUserId(userId).stream()
                .map(communityAppMapper::toReferralView)
                .collect(Collectors.toList());

        long pending = referralRepository.countByReferrerUserIdAndStatus(userId, ReferralStatus.PENDING);
        long completed = referralRepository.countByReferrerUserIdAndStatus(userId, ReferralStatus.COMPLETED);
        long rewarded = referralRepository.countByReferrerUserIdAndStatus(userId, ReferralStatus.REWARDED);

        return MyReferralsView.builder()
                .referredBy(referredBy)
                .referredUsers(referredUsers)
                .pendingCount(pending)
                .completedCount(completed)
                .rewardedCount(rewarded)
                .build();
    }
}
