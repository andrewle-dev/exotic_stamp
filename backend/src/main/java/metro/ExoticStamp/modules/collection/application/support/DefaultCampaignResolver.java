package metro.ExoticStamp.modules.collection.application.support;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotActiveException;
import metro.ExoticStamp.modules.collection.domain.exception.DefaultCampaignAmbiguousException;
import metro.ExoticStamp.modules.collection.domain.exception.DefaultCampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * MVP policy: one global active default campaign ({@code is_default=true}, ACTIVE, in time window).
 * {@code optionalLineId} disambiguates only when multiple active defaults exist.
 *
 * <p>Future: per-line defaults or user-selected campaign — extend with explicit selection APIs;
 * do not overload this resolver with multi-campaign user choice.
 */
@Component
@RequiredArgsConstructor
public class DefaultCampaignResolver {

    private final CampaignRepository campaignRepository;
    private final Clock clock;

    /**
     * Resolves the single active global default campaign for collection runtime.
     *
     * @param optionalLineId optional filter when multiple active defaults exist
     */
    public Campaign resolveActiveGlobalDefault(UUID optionalLineId) {
        List<Campaign> inWindow = activeDefaultsInWindow();
        if (inWindow.isEmpty()) {
            throw new DefaultCampaignNotFoundException(optionalLineId);
        }
        if (inWindow.size() == 1) {
            return assertCollectable(inWindow.getFirst());
        }
        if (optionalLineId == null) {
            throw new DefaultCampaignAmbiguousException(inWindow.size());
        }
        List<Campaign> forLine = inWindow.stream()
                .filter(c -> optionalLineId.equals(c.getLineId()))
                .toList();
        if (forLine.isEmpty()) {
            throw new DefaultCampaignNotFoundException(optionalLineId);
        }
        if (forLine.size() == 1) {
            return assertCollectable(forLine.getFirst());
        }
        throw new DefaultCampaignAmbiguousException(forLine.size());
    }

    /** @deprecated Use {@link #resolveActiveGlobalDefault(UUID)}. */
    @Deprecated
    public Campaign resolveActiveDefault(UUID lineId) {
        return resolveActiveGlobalDefault(lineId);
    }

    /** @deprecated Use {@link #resolveActiveGlobalDefault(UUID)}. */
    @Deprecated
    public Campaign resolveActiveDefaultOptionalLine(UUID lineId) {
        return resolveActiveGlobalDefault(lineId);
    }

    private List<Campaign> activeDefaultsInWindow() {
        return campaignRepository.findAllActiveDefaults().stream()
                .filter(this::isInWindow)
                .toList();
    }

    private Campaign assertCollectable(Campaign campaign) {
        if (!campaign.isActiveForCollection()) {
            throw new CampaignNotActiveException(campaign.getId());
        }
        if (!isInWindow(campaign)) {
            throw new CampaignNotActiveException(campaign.getId());
        }
        return campaign;
    }

    private boolean isInWindow(Campaign campaign) {
        LocalDateTime now = LocalDateTime.now(clock);
        return !now.isBefore(campaign.getStartAt()) && !now.isAfter(campaign.getEndAt());
    }
}
