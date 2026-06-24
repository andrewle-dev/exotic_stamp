package metro.ExoticStamp.modules.reward.domain.repository;

import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VoucherPoolRepository {

    List<VoucherPool> saveAll(Iterable<VoucherPool> vouchers);

    VoucherPool save(VoucherPool voucher);

    Optional<VoucherPool> findById(UUID id);

    Optional<VoucherPool> findByCode(String code);

    Optional<VoucherPool> lockNextAvailableForMilestone(UUID milestoneId);

    /** @deprecated Use {@link #lockNextAvailableForMilestone(UUID)}. */
    @Deprecated
    Optional<VoucherPool> lockNextAvailableForReward(UUID rewardId);

    long countAvailableByMilestoneId(UUID milestoneId);

    PagedSlice<VoucherPool> findByMilestoneIdPaged(UUID milestoneId, VoucherPoolStatus status, int page, int size);

    /** @deprecated Legacy reward-scoped stats. */
    @Deprecated
    long countAvailableByRewardId(UUID rewardId);

    /** @deprecated Legacy reward-scoped stats. */
    @Deprecated
    long countRedeemedByRewardId(UUID rewardId);
}
