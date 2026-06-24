package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.command.CreatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.CreateRewardCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateRewardCommand;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.VoucherCodeExhaustedException;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import metro.ExoticStamp.modules.reward.domain.repository.RewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminRewardCommandService {

    private final PartnerRepository partnerRepository;
    private final MilestoneRepository milestoneRepository;
    private final RewardRepository rewardRepository;
    private final VoucherPoolRepository voucherPoolRepository;
    private final RewardAppMapper rewardAppMapper;

    @Transactional
    public PartnerView createPartner(CreatePartnerCommand cmd) {
        Partner p = Partner.builder()
                .name(cmd.name())
                .logoUrl(cmd.logoUrl())
                .contactEmail(cmd.contactEmail())
                .contractStartDate(cmd.contractStartDate())
                .contractEndDate(cmd.contractEndDate())
                .active(true)
                .build();
        return rewardAppMapper.toPartnerView(partnerRepository.save(p));
    }

    @Transactional
    public PartnerView updatePartner(UpdatePartnerCommand cmd) {
        Partner p = partnerRepository.findById(cmd.id())
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + cmd.id()));
        if (cmd.name() != null) {
            p.setName(cmd.name());
        }
        if (cmd.logoUrl() != null) {
            p.setLogoUrl(cmd.logoUrl());
        }
        if (cmd.contactEmail() != null) {
            p.setContactEmail(cmd.contactEmail());
        }
        if (cmd.contractStartDate() != null) {
            p.setContractStartDate(cmd.contractStartDate());
        }
        if (cmd.contractEndDate() != null) {
            p.setContractEndDate(cmd.contractEndDate());
        }
        return rewardAppMapper.toPartnerView(partnerRepository.save(p));
    }

    @Transactional
    public PartnerView activatePartner(UUID id) {
        Partner p = partnerRepository.findById(id)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + id));
        if (p.isActive()) {
            throw new PartnerAlreadyActiveException("Partner already active: " + id);
        }
        p.setActive(true);
        return rewardAppMapper.toPartnerView(partnerRepository.save(p));
    }

    @Transactional
    public PartnerView deactivatePartner(UUID id) {
        Partner p = partnerRepository.findById(id)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + id));
        if (!p.isActive()) {
            throw new PartnerAlreadyInactiveException("Partner already inactive: " + id);
        }
        p.setActive(false);
        return rewardAppMapper.toPartnerView(partnerRepository.save(p));
    }

    @Transactional
    @Deprecated
    public MilestoneView createMilestone(CreateMilestoneCommand cmd) {
        throw new UnsupportedOperationException("Use MilestoneCommandService via /api/v1/admin/rewards/milestones");
    }

    @Transactional
    @Deprecated
    public MilestoneView updateMilestone(UpdateMilestoneCommand cmd) {
        throw new UnsupportedOperationException("Use MilestoneCommandService via /api/v1/admin/rewards/milestones");
    }

    @Transactional
    @Deprecated
    public MilestoneView activateMilestone(UUID id) {
        throw new UnsupportedOperationException("Use MilestoneCommandService via /api/v1/admin/rewards/milestones");
    }

    @Transactional
    @Deprecated
    public MilestoneView deactivateMilestone(UUID id) {
        throw new UnsupportedOperationException("Use MilestoneCommandService via /api/v1/admin/rewards/milestones");
    }

    @Transactional
    public RewardView createReward(CreateRewardCommand cmd) {
        if (!milestoneRepository.existsById(cmd.milestoneId())) {
            throw new MilestoneNotFoundException("Milestone not found: " + cmd.milestoneId());
        }
        if (cmd.partnerId() != null && !partnerRepository.existsById(cmd.partnerId())) {
            throw new PartnerNotFoundException("Partner not found: " + cmd.partnerId());
        }
        Reward r = Reward.builder()
                .milestoneId(cmd.milestoneId())
                .partnerId(cmd.partnerId())
                .rewardType(RewardType.valueOf(cmd.rewardType()))
                .name(cmd.name())
                .description(cmd.description())
                .valueAmount(cmd.valueAmount())
                .expiryDays(cmd.expiryDays())
                .totalStock(cmd.totalStock())
                .issuedCount(0)
                .active(true)
                .build();
        return rewardAppMapper.toRewardView(rewardRepository.save(r));
    }

    @Transactional
    public RewardView updateReward(UpdateRewardCommand cmd) {
        Reward r = rewardRepository.findById(cmd.id())
                .orElseThrow(() -> new RewardNotFoundException("Reward not found: " + cmd.id()));
        if (cmd.milestoneId() != null) {
            if (!milestoneRepository.existsById(cmd.milestoneId())) {
                throw new MilestoneNotFoundException("Milestone not found: " + cmd.milestoneId());
            }
            r.setMilestoneId(cmd.milestoneId());
        }
        if (cmd.partnerId() != null) {
            if (!partnerRepository.existsById(cmd.partnerId())) {
                throw new PartnerNotFoundException("Partner not found: " + cmd.partnerId());
            }
            r.setPartnerId(cmd.partnerId());
        }
        if (cmd.rewardType() != null) {
            r.setRewardType(RewardType.valueOf(cmd.rewardType()));
        }
        if (cmd.name() != null) {
            r.setName(cmd.name());
        }
        if (cmd.description() != null) {
            r.setDescription(cmd.description());
        }
        if (cmd.valueAmount() != null) {
            r.setValueAmount(cmd.valueAmount());
        }
        if (cmd.expiryDays() != null) {
            r.setExpiryDays(cmd.expiryDays());
        }
        if (cmd.totalStock() != null) {
            r.setTotalStock(cmd.totalStock());
        }
        return rewardAppMapper.toRewardView(rewardRepository.save(r));
    }

    @Transactional
    public RewardView activateReward(UUID id) {
        Reward r = rewardRepository.findById(id)
                .orElseThrow(() -> new RewardNotFoundException("Reward not found: " + id));
        if (r.isActive()) {
            throw new RewardAlreadyActiveException("Reward already active: " + id);
        }
        r.setActive(true);
        return rewardAppMapper.toRewardView(rewardRepository.save(r));
    }

    @Transactional
    public RewardView deactivateReward(UUID id) {
        Reward r = rewardRepository.findById(id)
                .orElseThrow(() -> new RewardNotFoundException("Reward not found: " + id));
        if (!r.isActive()) {
            throw new RewardAlreadyInactiveException("Reward already inactive: " + id);
        }
        r.setActive(false);
        return rewardAppMapper.toRewardView(rewardRepository.save(r));
    }

    @Transactional
    public int bulkUploadVouchers(UUID rewardId, List<String> codes) {
        if (!rewardRepository.existsById(rewardId)) {
            throw new RewardNotFoundException("Reward not found: " + rewardId);
        }
        if (codes == null || codes.isEmpty()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        List<VoucherPool> batch = new ArrayList<>();
        for (String code : codes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            batch.add(VoucherPool.builder()
                    .rewardId(rewardId)
                    .code(code.trim())
                    .status(VoucherPoolStatus.AVAILABLE)
                    .redeemed(false)
                    .createdAt(now)
                    .build());
        }
        try {
            voucherPoolRepository.saveAll(batch);
        } catch (DataIntegrityViolationException ex) {
            String msg = ex.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("uq_voucher_pool_code")) {
                throw new VoucherCodeExhaustedException("Duplicate voucher code in upload or existing pool");
            }
            throw ex;
        }
        return batch.size();
    }
}
