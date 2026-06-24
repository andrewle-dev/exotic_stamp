package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.support.RewardEnumParser;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.application.view.UserRewardVoucherView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserRewardQueryService {

    private final UserRewardRepository userRewardRepository;
    private final MilestoneRepository milestoneRepository;
    private final VoucherPoolRepository voucherPoolRepository;
    private final RewardAppMapper rewardAppMapper;
    private final RewardProperties rewardProperties;

    public PageResponse<UserRewardView> listMyRewards(UUID userId, String status, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        RewardStatus rewardStatus = RewardEnumParser.parseRewardStatus(status);
        PagedSlice<UserReward> slice = rewardStatus == null
                ? userRewardRepository.findByUserIdOrderByIssuedAtDesc(userId, p, s)
                : userRewardRepository.findByUserIdAndStatusOrderByIssuedAtDesc(userId, rewardStatus, p, s);
        Map<UUID, Milestone> milestoneMap = loadMilestones(slice.content());
        List<UserRewardView> content = slice.content().stream()
                .map(ur -> toView(ur, milestoneMap.get(ur.getMilestoneId()), userId, false))
                .collect(Collectors.toList());
        return PageResponse.of(content, slice.totalElements(), slice.totalPages(), slice.page(), slice.size());
    }

    public UserRewardView getMyReward(UUID userId, UUID userRewardId) {
        UserReward ur = userRewardRepository.findByUserIdAndId(userId, userRewardId)
                .orElseThrow(() -> new RewardNotFoundException("User reward not found: " + userRewardId));
        Milestone milestone = milestoneRepository.findById(ur.getMilestoneId()).orElse(null);
        return toView(ur, milestone, userId, true);
    }

    private UserRewardView toView(UserReward ur, Milestone milestone, UUID requestingUserId, boolean includeVoucherCode) {
        UserRewardVoucherView voucher = null;
        if (includeVoucherCode
                && ur.getStatus() == RewardStatus.ISSUED
                && ur.getVoucherPoolId() != null
                && requestingUserId.equals(ur.getUserId())) {
            Optional<VoucherPool> vp = voucherPoolRepository.findById(ur.getVoucherPoolId());
            if (vp.isPresent() && requestingUserId.equals(vp.get().getAssignedUserId())) {
                voucher = UserRewardVoucherView.builder()
                        .id(vp.get().getId())
                        .code(vp.get().getCode())
                        .build();
            }
        }
        return rewardAppMapper.toUserRewardView(ur, milestone, voucher);
    }

    private Map<UUID, Milestone> loadMilestones(List<UserReward> list) {
        Set<UUID> ids = list.stream().map(UserReward::getMilestoneId).collect(Collectors.toSet());
        Map<UUID, Milestone> map = new HashMap<>();
        for (UUID id : ids) {
            milestoneRepository.findById(id).ifPresent(m -> map.put(id, m));
        }
        return map;
    }

    private int normalizeSize(int size) {
        int max = rewardProperties.getMaxPageSize();
        int def = rewardProperties.getDefaultPageSize();
        if (size <= 0) {
            return def;
        }
        return Math.min(size, max);
    }
}
