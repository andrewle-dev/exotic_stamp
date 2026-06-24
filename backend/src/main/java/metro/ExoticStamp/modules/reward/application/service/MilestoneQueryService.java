package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.support.RewardEnumParser;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MilestoneQueryService {

    private final MilestoneRepository milestoneRepository;
    private final RewardAppMapper rewardAppMapper;
    private final RewardProperties rewardProperties;

    public PageResponse<MilestoneView> list(UUID campaignId, String status, int page, int size) {
        int p = Math.max(0, page);
        int s = normalizeSize(size);
        MilestoneStatus milestoneStatus = RewardEnumParser.parseMilestoneStatus(status);
        PagedSlice<metro.ExoticStamp.modules.reward.domain.model.Milestone> slice =
                milestoneRepository.findAllNotDeletedPaged(campaignId, milestoneStatus, p, s);
        List<MilestoneView> content = slice.content().stream()
                .map(rewardAppMapper::toMilestoneView)
                .collect(Collectors.toList());
        return PageResponse.of(content, slice.totalElements(), slice.totalPages(), slice.page(), slice.size());
    }

    public MilestoneView get(UUID id) {
        return milestoneRepository.findByIdNotDeleted(id)
                .map(rewardAppMapper::toMilestoneView)
                .orElseThrow(() -> new MilestoneNotFoundException("Milestone not found: " + id));
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
