package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.support.RewardEnumParser;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherPoolQueryService {

    private final VoucherPoolRepository voucherPoolRepository;
    private final RewardAppMapper rewardAppMapper;
    private final RewardProperties rewardProperties;

    public PageResponse<VoucherPoolView> list(UUID milestoneId, String status, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        VoucherPoolStatus poolStatus = RewardEnumParser.parseVoucherPoolStatus(status);
        PagedSlice<VoucherPool> slice = voucherPoolRepository.findByMilestoneIdPaged(milestoneId, poolStatus, p, s);
        List<VoucherPoolView> content = slice.content().stream()
                .map(rewardAppMapper::toVoucherPoolView)
                .collect(Collectors.toList());
        return PageResponse.of(content, slice.totalElements(), slice.totalPages(), slice.page(), slice.size());
    }

    public VoucherPoolView get(UUID id) {
        return voucherPoolRepository.findById(id)
                .map(rewardAppMapper::toVoucherPoolView)
                .orElseThrow(() -> new InvalidMilestoneStateException("Voucher not found: " + id));
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
