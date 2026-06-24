package metro.ExoticStamp.modules.reward.domain.service;

import metro.ExoticStamp.modules.reward.domain.model.Milestone;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Pure milestone completion rules: which thresholds are satisfied and not yet rewarded.
 */
public final class MilestoneDomainService {

    public List<Milestone> findNewlyCompletedMilestones(
            long currentDistinctStampCount,
            List<Milestone> applicableActiveMilestones,
            Set<UUID> alreadyRewardedMilestoneIds
    ) {
        if (applicableActiveMilestones == null || applicableActiveMilestones.isEmpty()) {
            return List.of();
        }
        Set<UUID> rewarded = alreadyRewardedMilestoneIds == null ? Set.of() : alreadyRewardedMilestoneIds;
        return applicableActiveMilestones.stream()
                .filter(m -> m.getStampsRequired() != null && m.getStampsRequired() <= currentDistinctStampCount)
                .filter(m -> m.getId() != null && !rewarded.contains(m.getId()))
                .sorted(Comparator.comparingInt(Milestone::getStampsRequired))
                .collect(Collectors.toList());
    }
}
