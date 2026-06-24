package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.UserReward;
import metro.ExoticStamp.modules.reward.domain.repository.UserRewardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRewardRepositoryAdapter implements UserRewardRepository {

    private final JpaUserRewardRepository jpaUserRewardRepository;

    @Override
    public UserReward save(UserReward userReward) {
        return jpaUserRewardRepository.save(userReward);
    }

    @Override
    public Optional<UserReward> findByUserIdAndId(UUID userId, UUID id) {
        return jpaUserRewardRepository.findByUserIdAndId(userId, id);
    }

    @Override
    public Set<UUID> findMilestoneIdsRewardedForUser(UUID userId) {
        return jpaUserRewardRepository.findDistinctMilestoneIdsByUserId(userId);
    }

    @Override
    public boolean existsByUserIdAndMilestoneId(UUID userId, UUID milestoneId) {
        return jpaUserRewardRepository.existsByUserIdAndMilestoneId(userId, milestoneId);
    }

    @Override
    public PagedSlice<UserReward> findByUserIdOrderByIssuedAtDesc(UUID userId, int page, int size) {
        Page<UserReward> p = jpaUserRewardRepository.findByUserIdOrderByIssuedAtDesc(userId, PageRequest.of(page, size));
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    public PagedSlice<UserReward> findByUserIdAndStatusOrderByIssuedAtDesc(UUID userId, RewardStatus status, int page, int size) {
        Page<UserReward> p = jpaUserRewardRepository.findByUserIdAndStatusOrderByIssuedAtDesc(
                userId, status, PageRequest.of(page, size));
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    public int expireIssuedBefore(LocalDateTime now) {
        return jpaUserRewardRepository.expireIssuedBefore(now);
    }
}
