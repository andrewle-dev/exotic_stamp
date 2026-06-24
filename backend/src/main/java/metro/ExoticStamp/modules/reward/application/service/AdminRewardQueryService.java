package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolStatsView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import metro.ExoticStamp.modules.reward.domain.repository.RewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRewardQueryService {

    private final PartnerRepository partnerRepository;
    private final MilestoneRepository milestoneRepository;
    private final RewardRepository rewardRepository;
    private final VoucherPoolRepository voucherPoolRepository;
    private final RewardAppMapper rewardAppMapper;
    private final RewardProperties rewardProperties;

    public PageResponse<PartnerView> listPartners(Boolean activeOnly, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        PagedSlice<metro.ExoticStamp.modules.reward.domain.model.Partner> slice =
                partnerRepository.findAllPaged(activeOnly, p, s);
        List<PartnerView> content = slice.content().stream()
                .map(rewardAppMapper::toPartnerView)
                .collect(Collectors.toList());
        return PageResponse.of(content, slice.totalElements(), slice.totalPages(), slice.page(), slice.size());
    }

    public PartnerView getPartner(UUID id) {
        return partnerRepository.findById(id)
                .map(rewardAppMapper::toPartnerView)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + id));
    }

    public PageResponse<MilestoneView> listMilestones(Boolean activeOnly, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        PagedSlice<metro.ExoticStamp.modules.reward.domain.model.Milestone> slice =
                milestoneRepository.findAllPaged(activeOnly, p, s);
        List<MilestoneView> content = slice.content().stream()
                .map(rewardAppMapper::toMilestoneView)
                .collect(Collectors.toList());
        return PageResponse.of(content, slice.totalElements(), slice.totalPages(), slice.page(), slice.size());
    }

    public MilestoneView getMilestone(UUID id) {
        return milestoneRepository.findById(id)
                .map(rewardAppMapper::toMilestoneView)
                .orElseThrow(() -> new MilestoneNotFoundException("Milestone not found: " + id));
    }

    public PageResponse<RewardView> listRewards(Boolean activeOnly, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        PagedSlice<metro.ExoticStamp.modules.reward.domain.model.Reward> slice =
                rewardRepository.findAllPaged(activeOnly, p, s);
        List<RewardView> content = slice.content().stream()
                .map(rewardAppMapper::toRewardView)
                .collect(Collectors.toList());
        return PageResponse.of(content, slice.totalElements(), slice.totalPages(), slice.page(), slice.size());
    }

    public RewardView getReward(UUID id) {
        return rewardRepository.findById(id)
                .map(rewardAppMapper::toRewardView)
                .orElseThrow(() -> new RewardNotFoundException("Reward not found: " + id));
    }

    public VoucherPoolStatsView getVoucherStats(UUID rewardId) {
        if (!rewardRepository.existsById(rewardId)) {
            throw new RewardNotFoundException("Reward not found: " + rewardId);
        }
        long available = voucherPoolRepository.countAvailableByRewardId(rewardId);
        long redeemed = voucherPoolRepository.countRedeemedByRewardId(rewardId);
        return VoucherPoolStatsView.builder()
                .availableCount(available)
                .redeemedCount(redeemed)
                .build();
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
