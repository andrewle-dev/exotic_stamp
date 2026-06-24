package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.application.command.CreateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateMilestoneCommand;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.application.support.RewardEnumParser;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneArchivedException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneCodeDuplicateException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MilestoneCommandService {

    private final MilestoneRepository milestoneRepository;
    private final RewardAppMapper rewardAppMapper;
    private final RewardAuditHelper rewardAuditHelper;
    private final Clock clock;

    @Transactional
    public MilestoneView create(CreateMilestoneCommand cmd) {
        if (cmd.campaignId() == null) {
            throw new InvalidMilestoneStateException("campaignId is required");
        }
        if (milestoneRepository.existsByCampaignIdAndCodeAndIdNot(cmd.campaignId(), cmd.code(), null)) {
            throw new MilestoneCodeDuplicateException("Milestone code already exists: " + cmd.code());
        }
        MilestoneStatus status = RewardEnumParser.parseMilestoneStatus(cmd.status());
        if (status == null) {
            status = MilestoneStatus.DRAFT;
        }
        RewardType rewardType = RewardEnumParser.parseRewardType(cmd.rewardType());
        if (rewardType == null) {
            throw new InvalidMilestoneStateException("rewardType is required");
        }
        Milestone m = Milestone.builder()
                .campaignId(cmd.campaignId())
                .code(cmd.code())
                .stampsRequired(cmd.requiredStampCount())
                .name(cmd.name())
                .description(cmd.description())
                .rewardType(rewardType)
                .rewardTitle(cmd.rewardTitle())
                .rewardDescription(cmd.rewardDescription())
                .rewardImageUrl(cmd.rewardImageUrl())
                .status(status)
                .sortOrder(cmd.sortOrder())
                .build();
        Milestone saved = milestoneRepository.save(m);
        rewardAuditHelper.scheduleMilestoneCreated(saved.getId());
        return rewardAppMapper.toMilestoneView(saved);
    }

    @Transactional
    public MilestoneView update(UpdateMilestoneCommand cmd) {
        Milestone m = milestoneRepository.findByIdNotDeleted(cmd.id())
                .orElseThrow(() -> new MilestoneNotFoundException("Milestone not found: " + cmd.id()));
        assertMutable(m);
        if (cmd.code() != null) {
            if (milestoneRepository.existsByCampaignIdAndCodeAndIdNot(m.getCampaignId(), cmd.code(), m.getId())) {
                throw new MilestoneCodeDuplicateException("Milestone code already exists: " + cmd.code());
            }
            m.setCode(cmd.code());
        }
        if (cmd.requiredStampCount() != null) {
            m.setStampsRequired(cmd.requiredStampCount());
        }
        if (cmd.name() != null) {
            m.setName(cmd.name());
        }
        if (cmd.description() != null) {
            m.setDescription(cmd.description());
        }
        if (cmd.rewardType() != null) {
            m.setRewardType(RewardEnumParser.parseRewardType(cmd.rewardType()));
        }
        if (cmd.rewardTitle() != null) {
            m.setRewardTitle(cmd.rewardTitle());
        }
        if (cmd.rewardDescription() != null) {
            m.setRewardDescription(cmd.rewardDescription());
        }
        if (cmd.rewardImageUrl() != null) {
            m.setRewardImageUrl(cmd.rewardImageUrl());
        }
        if (cmd.status() != null) {
            m.setStatus(RewardEnumParser.parseMilestoneStatus(cmd.status()));
        }
        if (cmd.sortOrder() != null) {
            m.setSortOrder(cmd.sortOrder());
        }
        Milestone saved = milestoneRepository.save(m);
        rewardAuditHelper.scheduleMilestoneUpdated(saved.getId());
        return rewardAppMapper.toMilestoneView(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Milestone m = milestoneRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new MilestoneNotFoundException("Milestone not found: " + id));
        assertMutable(m);
        m.setStatus(MilestoneStatus.INACTIVE);
        m.setDeletedAt(LocalDateTime.now(clock));
        milestoneRepository.save(m);
        rewardAuditHelper.scheduleMilestoneDisabled(id);
    }

    private static void assertMutable(Milestone m) {
        if (m.isArchived()) {
            throw new MilestoneArchivedException(m.getId());
        }
    }
}
