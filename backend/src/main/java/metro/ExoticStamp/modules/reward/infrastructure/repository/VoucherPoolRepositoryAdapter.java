package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VoucherPoolRepositoryAdapter implements VoucherPoolRepository {

    private final JpaVoucherPoolRepository jpaVoucherPoolRepository;

    @Override
    public List<VoucherPool> saveAll(Iterable<VoucherPool> vouchers) {
        return jpaVoucherPoolRepository.saveAll(vouchers);
    }

    @Override
    public VoucherPool save(VoucherPool voucher) {
        return jpaVoucherPoolRepository.save(voucher);
    }

    @Override
    public Optional<VoucherPool> findById(UUID id) {
        return jpaVoucherPoolRepository.findById(id);
    }

    @Override
    public Optional<VoucherPool> findByCode(String code) {
        return jpaVoucherPoolRepository.findByCode(code);
    }

    @Override
    public Optional<VoucherPool> lockNextAvailableForMilestone(UUID milestoneId) {
        return jpaVoucherPoolRepository.lockNextAvailableForMilestone(milestoneId);
    }

    @Override
    public Optional<VoucherPool> lockNextAvailableForReward(UUID rewardId) {
        return jpaVoucherPoolRepository.lockNextAvailable(rewardId);
    }

    @Override
    public long countAvailableByMilestoneId(UUID milestoneId) {
        return jpaVoucherPoolRepository.countUnissuedAvailableByMilestoneId(milestoneId);
    }

    @Override
    public PagedSlice<VoucherPool> findByMilestoneIdPaged(UUID milestoneId, VoucherPoolStatus status, int page, int size) {
        Page<VoucherPool> p = jpaVoucherPoolRepository.findFiltered(
                milestoneId, status, PageRequest.of(page, size));
        return new PagedSlice<>(p.getContent(), p.getTotalElements(), p.getTotalPages(), p.getNumber(), p.getSize());
    }

    @Override
    @Deprecated
    public long countAvailableByRewardId(UUID rewardId) {
        return jpaVoucherPoolRepository.countUnissuedAvailableByRewardId(rewardId);
    }

    @Override
    @Deprecated
    public long countRedeemedByRewardId(UUID rewardId) {
        return jpaVoucherPoolRepository.countRedeemedByRewardId(rewardId);
    }
}
