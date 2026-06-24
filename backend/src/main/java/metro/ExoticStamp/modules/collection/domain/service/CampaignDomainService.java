package metro.ExoticStamp.modules.collection.domain.service;

import metro.ExoticStamp.modules.collection.domain.exception.CampaignArchivedException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
public class CampaignDomainService {

    public void validateDateRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null) {
            throw new InvalidRequestException("startAt and endAt are required");
        }
        if (!startAt.isBefore(endAt)) {
            throw new InvalidRequestException("startAt must be before endAt");
        }
    }

    public void assertMutable(Campaign campaign) {
        if (campaign.isDeleted()) {
            throw new InvalidRequestException("Campaign is deleted");
        }
        if (campaign.isArchived()) {
            throw new CampaignArchivedException(campaign.getId());
        }
    }

    public void assertCanActivate(Campaign campaign, int stationCount, Clock clock) {
        validateDateRange(campaign.getStartAt(), campaign.getEndAt());
        if (stationCount < 1) {
            throw new InvalidRequestException("Campaign must have at least one station assigned before activation");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!campaign.getEndAt().isAfter(now)) {
            throw new InvalidRequestException("Campaign endAt must be in the future to activate");
        }
    }

    public void rejectReactivationFromArchived(Campaign campaign, CampaignStatus newStatus) {
        if (campaign.getStatus() == CampaignStatus.ARCHIVED && newStatus == CampaignStatus.ACTIVE) {
            throw new CampaignArchivedException(campaign.getId());
        }
    }
}
