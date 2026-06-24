package metro.ExoticStamp.modules.reward.application.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherAllocationService {

    private final VoucherPoolRepository voucherPoolRepository;
    private final Clock clock;

    @Builder
    public record VoucherAllocation(UUID voucherPoolId) {
    }

    @Transactional
    public Optional<VoucherAllocation> allocate(UUID milestoneId, UUID userId, UUID userRewardId) {
        Optional<VoucherPool> locked = voucherPoolRepository.lockNextAvailableForMilestone(milestoneId);
        if (locked.isEmpty()) {
            return Optional.empty();
        }
        VoucherPool vp = locked.get();
        vp.setStatus(VoucherPoolStatus.CLAIMED);
        vp.setAssignedUserId(userId);
        vp.setAssignedUserRewardId(userRewardId);
        vp.setAssignedAt(LocalDateTime.now(clock));
        voucherPoolRepository.save(vp);
        return Optional.of(new VoucherAllocation(vp.getId()));
    }

    @Transactional
    public void release(UUID voucherPoolId) {
        voucherPoolRepository.findById(voucherPoolId).ifPresent(vp -> {
            if (vp.getStatus() == VoucherPoolStatus.CLAIMED) {
                vp.setStatus(VoucherPoolStatus.AVAILABLE);
                vp.setAssignedUserId(null);
                vp.setAssignedUserRewardId(null);
                vp.setAssignedAt(null);
                voucherPoolRepository.save(vp);
            }
        });
    }
}
