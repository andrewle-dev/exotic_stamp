package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.command.CreateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.command.UpdateStampDesignCommand;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.support.CollectionEnumParser;
import metro.ExoticStamp.modules.collection.application.support.CampaignAuditHelper;
import metro.ExoticStamp.modules.collection.application.view.StampDesignView;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.DuplicateActiveStampDesignException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.service.StampDesignDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StampDesignCommandService {

    private final StampDesignRepository stampDesignRepository;
    private final CampaignRepository campaignRepository;
    private final StampDesignDomainService stampDesignDomainService;
    private final CampaignAppMapper campaignAppMapper;
    private final CampaignAuditHelper campaignAuditHelper;
    private final Clock clock;

    @Transactional
    public StampDesignView create(CreateStampDesignCommand cmd) {
        campaignRepository.findByIdNotDeleted(cmd.campaignId())
                .orElseThrow(() -> new CampaignNotFoundException(cmd.campaignId()));
        stampDesignDomainService.assertStationInCampaign(cmd.campaignId(), cmd.stationId());

        StampDesignStatus status = CollectionEnumParser.parseStampDesignStatus(cmd.status());
        if (status == null) {
            status = StampDesignStatus.DRAFT;
        }
        assertNoDuplicateActive(cmd.campaignId(), cmd.stationId(), null, status);

        int sortOrder = cmd.sortOrder() != null ? cmd.sortOrder() : 0;
        if (sortOrder < 0) {
            throw new InvalidRequestException("sortOrder must be >= 0");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        StampDesign entity = StampDesign.builder()
                .campaignId(cmd.campaignId())
                .stationId(cmd.stationId())
                .name(cmd.name().trim())
                .description(cmd.description())
                .imageUrl(cmd.imageUrl().trim())
                .previewImageUrl(cmd.previewImageUrl())
                .rarity(CollectionEnumParser.parseRarity(cmd.rarity()) != null
                        ? CollectionEnumParser.parseRarity(cmd.rarity()) : StampRarity.COMMON)
                .status(status)
                .sortOrder(sortOrder)
                .isLimited(false)
                .createdAt(now)
                .build();
        StampDesign saved = stampDesignRepository.save(entity);
        campaignAuditHelper.scheduleStampDesignCreated(saved);
        return campaignAppMapper.toStampDesignView(saved);
    }

    @Transactional
    public StampDesignView update(UpdateStampDesignCommand cmd) {
        StampDesign entity = stampDesignRepository.findByIdNotDeleted(cmd.id())
                .orElseThrow(() -> new InvalidRequestException("Stamp design not found: " + cmd.id()));

        UUID campaignId = cmd.campaignId() != null ? cmd.campaignId() : entity.getCampaignId();
        UUID stationId = cmd.stationId() != null ? cmd.stationId() : entity.getStationId();

        campaignRepository.findByIdNotDeleted(campaignId)
                .orElseThrow(() -> new CampaignNotFoundException(campaignId));
        stampDesignDomainService.assertStationInCampaign(campaignId, stationId);

        StampDesignStatus previousStatus = entity.getStatus();
        StampDesignStatus newStatus = cmd.status() != null
                ? CollectionEnumParser.parseStampDesignStatus(cmd.status()) : entity.getStatus();
        assertNoDuplicateActive(campaignId, stationId, entity.getId(), newStatus);

        if (cmd.name() != null) {
            entity.setName(cmd.name().trim());
        }
        if (cmd.description() != null) {
            entity.setDescription(cmd.description());
        }
        if (cmd.imageUrl() != null) {
            entity.setImageUrl(cmd.imageUrl().trim());
        }
        if (cmd.previewImageUrl() != null) {
            entity.setPreviewImageUrl(cmd.previewImageUrl());
        }
        if (cmd.rarity() != null) {
            entity.setRarity(CollectionEnumParser.parseRarity(cmd.rarity()));
        }
        if (cmd.sortOrder() != null) {
            if (cmd.sortOrder() < 0) {
                throw new InvalidRequestException("sortOrder must be >= 0");
            }
            entity.setSortOrder(cmd.sortOrder());
        }
        entity.setCampaignId(campaignId);
        entity.setStationId(stationId);
        entity.setStatus(newStatus);

        StampDesign saved = stampDesignRepository.save(entity);
        if (newStatus == StampDesignStatus.INACTIVE && previousStatus != StampDesignStatus.INACTIVE) {
            campaignAuditHelper.scheduleStampDesignDisabled(saved.getId());
        } else {
            campaignAuditHelper.scheduleStampDesignUpdated(saved);
        }
        return campaignAppMapper.toStampDesignView(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        StampDesign entity = stampDesignRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new InvalidRequestException("Stamp design not found: " + id));
        entity.setDeletedAt(LocalDateTime.now(clock));
        stampDesignRepository.save(entity);
    }

    private void assertNoDuplicateActive(UUID campaignId, UUID stationId, UUID excludeId, StampDesignStatus status) {
        if (status != StampDesignStatus.ACTIVE) {
            return;
        }
        boolean duplicate = excludeId == null
                ? stampDesignRepository.existsActiveByCampaignIdAndStationId(campaignId, stationId)
                : stampDesignRepository.existsActiveByCampaignIdAndStationIdAndIdNot(campaignId, stationId, excludeId);
        if (duplicate) {
            throw new DuplicateActiveStampDesignException();
        }
    }
}
