package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.command.CreateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.UpdateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.support.CollectionEnumParser;
import metro.ExoticStamp.modules.collection.application.support.CampaignAuditHelper;
import metro.ExoticStamp.modules.collection.application.view.CampaignView;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignCodeDuplicateException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.service.CampaignDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignCommandService {

    private final CampaignRepository campaignRepository;
    private final CampaignStationRepository campaignStationRepository;
    private final CampaignDomainService campaignDomainService;
    private final CampaignAppMapper campaignAppMapper;
    private final CampaignAuditHelper campaignAuditHelper;
    private final Clock clock;

    @Transactional
    public CampaignView create(CreateCampaignCommand cmd) {
        String code = cmd.code().trim();
        if (campaignRepository.existsByCode(code)) {
            throw new CampaignCodeDuplicateException(code);
        }
        CampaignType type = CollectionEnumParser.parseCampaignType(cmd.campaignType());
        if (type == null) {
            type = CampaignType.STANDARD;
        }
        campaignDomainService.validateDateRange(cmd.startAt(), cmd.endAt());
        int priority = cmd.priority() != null ? cmd.priority() : 0;
        if (priority < 0) {
            throw new InvalidRequestException("priority must be >= 0");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        String displayName = cmd.displayName() != null && !cmd.displayName().isBlank()
                ? cmd.displayName().trim() : cmd.name().trim();
        Campaign campaign = Campaign.builder()
                .code(code)
                .name(cmd.name().trim())
                .displayName(displayName)
                .description(cmd.description())
                .campaignType(type)
                .status(CampaignStatus.DRAFT)
                .startAt(cmd.startAt())
                .endAt(cmd.endAt())
                .bannerImageUrl(cmd.bannerImageUrl())
                .thumbnailImageUrl(cmd.thumbnailImageUrl())
                .priority(priority)
                .isDefault(false)
                .createdAt(now)
                .build();
        Campaign saved = campaignRepository.save(campaign);
        campaignAuditHelper.scheduleCampaignCreated(saved);
        return campaignAppMapper.toCampaignView(saved);
    }

    @Transactional
    public CampaignView update(UpdateCampaignCommand cmd) {
        Campaign campaign = campaignRepository.findByIdNotDeleted(cmd.id())
                .orElseThrow(() -> new CampaignNotFoundException(cmd.id()));
        campaignDomainService.assertMutable(campaign);

        CampaignStatus previousStatus = campaign.getStatus();
        CampaignStatus newStatus = cmd.status() != null
                ? CollectionEnumParser.parseCampaignStatus(cmd.status()) : campaign.getStatus();
        campaignDomainService.rejectReactivationFromArchived(campaign, newStatus);

        if (cmd.code() != null) {
            String code = cmd.code().trim();
            if (campaignRepository.existsByCodeAndIdNot(code, cmd.id())) {
                throw new CampaignCodeDuplicateException(code);
            }
            campaign.setCode(code);
        }
        if (cmd.name() != null) {
            campaign.setName(cmd.name().trim());
        }
        if (cmd.displayName() != null) {
            campaign.setDisplayName(cmd.displayName().trim());
        }
        if (cmd.description() != null) {
            campaign.setDescription(cmd.description());
        }
        if (cmd.campaignType() != null) {
            campaign.setCampaignType(CollectionEnumParser.parseCampaignType(cmd.campaignType()));
        }
        if (cmd.startAt() != null) {
            campaign.setStartAt(cmd.startAt());
        }
        if (cmd.endAt() != null) {
            campaign.setEndAt(cmd.endAt());
        }
        campaignDomainService.validateDateRange(campaign.getStartAt(), campaign.getEndAt());

        if (cmd.bannerImageUrl() != null) {
            campaign.setBannerImageUrl(cmd.bannerImageUrl());
        }
        if (cmd.thumbnailImageUrl() != null) {
            campaign.setThumbnailImageUrl(cmd.thumbnailImageUrl());
        }
        if (cmd.priority() != null) {
            if (cmd.priority() < 0) {
                throw new InvalidRequestException("priority must be >= 0");
            }
            campaign.setPriority(cmd.priority());
        }

        if (newStatus == CampaignStatus.ACTIVE && previousStatus != CampaignStatus.ACTIVE) {
            int stationCount = campaignStationRepository.countByCampaignId(campaign.getId());
            campaignDomainService.assertCanActivate(campaign, stationCount, clock);
            campaign.setStatus(CampaignStatus.ACTIVE);
        } else if (newStatus == CampaignStatus.ARCHIVED && previousStatus != CampaignStatus.ARCHIVED) {
            campaign.setStatus(CampaignStatus.ARCHIVED);
        } else if (newStatus != null) {
            campaign.setStatus(newStatus);
        }

        Campaign saved = campaignRepository.save(campaign);

        if (newStatus == CampaignStatus.ACTIVE && previousStatus != CampaignStatus.ACTIVE) {
            campaignAuditHelper.scheduleCampaignActivated(campaign.getId());
        } else if (newStatus == CampaignStatus.ARCHIVED && previousStatus != CampaignStatus.ARCHIVED) {
            campaignAuditHelper.scheduleCampaignArchived(campaign.getId());
        } else {
            campaignAuditHelper.scheduleCampaignUpdated(saved);
        }
        return campaignAppMapper.toCampaignView(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Campaign campaign = campaignRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
        campaignDomainService.assertMutable(campaign);
        campaign.setDeletedAt(LocalDateTime.now(clock));
        campaignRepository.save(campaign);
    }
}
