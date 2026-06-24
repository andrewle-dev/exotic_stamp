package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.repository.RewardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RewardRepositoryAdapter implements RewardRepository {

    private final JpaRewardRepository jpaRewardRepository;

    @Override
    public Optional<Reward> findById(UUID id) {
        return jpaRewardRepository.findById(id);
    }

    @Override
    public Reward save(Reward reward) {
        return jpaRewardRepository.save(reward);
    }

    @Override
    public Optional<Reward> findActiveByMilestoneId(UUID milestoneId) {
        return jpaRewardRepository.findByMilestoneIdAndActiveTrue(milestoneId);
    }

    @Override
    public boolean incrementIssuedCountIfStockAllows(UUID rewardId) {
        return jpaRewardRepository.incrementIssuedCountIfStockAllows(rewardId) > 0;
    }

    @Override
    public void decrementIssuedCount(UUID rewardId) {
        jpaRewardRepository.decrementIssuedCount(rewardId);
    }

    @Override
    public PagedSlice<Reward> findAllPaged(Boolean activeOnly, int page, int size) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Reward> p;
        if (activeOnly == null) {
            p = jpaRewardRepository.findAll(pr);
        } else {
            p = jpaRewardRepository.findByActive(activeOnly, pr);
        }
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    public List<Reward> findAllByIds(Iterable<UUID> rewardIds) {
        return jpaRewardRepository.findByIdIn(rewardIds);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRewardRepository.existsById(id);
    }
}
