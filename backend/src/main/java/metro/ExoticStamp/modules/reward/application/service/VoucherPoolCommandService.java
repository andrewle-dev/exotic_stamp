package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.application.command.ImportVouchersCommand;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolView;
import metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.VoucherCodeDuplicateException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherPoolCommandService {

    private final MilestoneRepository milestoneRepository;
    private final VoucherPoolRepository voucherPoolRepository;
    private final RewardAppMapper rewardAppMapper;
    private final RewardAuditHelper rewardAuditHelper;
    private final Clock clock;

    @Transactional
    public int importVouchers(ImportVouchersCommand cmd) {
        Milestone milestone = milestoneRepository.findByIdNotDeleted(cmd.milestoneId())
                .orElseThrow(() -> new MilestoneNotFoundException("Milestone not found: " + cmd.milestoneId()));
        if (milestone.getRewardType() != RewardType.VOUCHER) {
            throw new InvalidMilestoneStateException("Milestone rewardType must be VOUCHER");
        }
        if (cmd.codes() == null || cmd.codes().isEmpty()) {
            return 0;
        }
        Set<String> seen = new HashSet<>();
        LocalDateTime now = LocalDateTime.now(clock);
        List<VoucherPool> batch = new ArrayList<>();
        for (String raw : cmd.codes()) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String code = raw.trim();
            if (!seen.add(code)) {
                throw new VoucherCodeDuplicateException("Duplicate voucher code in batch: " + code);
            }
            batch.add(VoucherPool.builder()
                    .milestoneId(cmd.milestoneId())
                    .code(code)
                    .status(VoucherPoolStatus.AVAILABLE)
                    .expiresAt(cmd.expiresAt())
                    .createdAt(now)
                    .build());
        }
        try {
            voucherPoolRepository.saveAll(batch);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("uq_voucher_pool_code")) {
                throw new VoucherCodeDuplicateException("Duplicate voucher code in upload or existing pool");
            }
            throw ex;
        }
        rewardAuditHelper.scheduleVoucherImported(cmd.milestoneId(), batch.size());
        return batch.size();
    }

    @Transactional
    public VoucherPoolView disable(UUID id) {
        VoucherPool vp = voucherPoolRepository.findById(id)
                .orElseThrow(() -> new InvalidMilestoneStateException("Voucher not found: " + id));
        if (vp.getStatus() == VoucherPoolStatus.CLAIMED) {
            throw new InvalidMilestoneStateException("Claimed voucher cannot be disabled");
        }
        vp.setStatus(VoucherPoolStatus.DISABLED);
        VoucherPool saved = voucherPoolRepository.save(vp);
        rewardAuditHelper.scheduleVoucherDisabled(id);
        return rewardAppMapper.toVoucherPoolView(saved);
    }
}
