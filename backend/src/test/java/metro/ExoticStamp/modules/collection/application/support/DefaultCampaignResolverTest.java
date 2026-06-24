package metro.ExoticStamp.modules.collection.application.support;

import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotActiveException;
import metro.ExoticStamp.modules.collection.domain.exception.DefaultCampaignAmbiguousException;
import metro.ExoticStamp.modules.collection.domain.exception.DefaultCampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCampaignResolverTest {

    private static final UUID LINE_ID = UUID.randomUUID();
    private static final UUID OTHER_LINE_ID = UUID.randomUUID();

    @Mock private CampaignRepository campaignRepository;

    private Clock clock;
    private DefaultCampaignResolver resolver;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-06-01T12:00:00Z"), ZoneOffset.UTC);
        resolver = new DefaultCampaignResolver(campaignRepository, clock);
    }

    @Test
    void resolveGlobalDefault_singleActiveDefault_withoutLineId() {
        Campaign campaign = activeCampaign(LINE_ID);
        when(campaignRepository.findAllActiveDefaults()).thenReturn(List.of(campaign));
        assertEquals(campaign, resolver.resolveActiveGlobalDefault(null));
    }

    @Test
    void resolveGlobalDefault_noneFound() {
        when(campaignRepository.findAllActiveDefaults()).thenReturn(List.of());
        assertThrows(DefaultCampaignNotFoundException.class, () -> resolver.resolveActiveGlobalDefault(null));
    }

    @Test
    void resolveGlobalDefault_multipleWithoutLineId_ambiguous() {
        when(campaignRepository.findAllActiveDefaults()).thenReturn(List.of(
                activeCampaign(LINE_ID),
                activeCampaign(OTHER_LINE_ID)));
        assertThrows(DefaultCampaignAmbiguousException.class, () -> resolver.resolveActiveGlobalDefault(null));
    }

    @Test
    void resolveGlobalDefault_multipleWithLineId_disambiguates() {
        Campaign forLine = activeCampaign(LINE_ID);
        when(campaignRepository.findAllActiveDefaults()).thenReturn(List.of(
                forLine,
                activeCampaign(OTHER_LINE_ID)));
        assertEquals(forLine, resolver.resolveActiveGlobalDefault(LINE_ID));
    }

    @Test
    void resolveGlobalDefault_multipleWithLineId_noMatch_notFound() {
        when(campaignRepository.findAllActiveDefaults()).thenReturn(List.of(
                activeCampaign(OTHER_LINE_ID),
                activeCampaign(UUID.randomUUID())));
        assertThrows(DefaultCampaignNotFoundException.class, () -> resolver.resolveActiveGlobalDefault(LINE_ID));
    }

    @Test
    void resolveGlobalDefault_expired_notInWindow() {
        Campaign campaign = activeCampaign(LINE_ID);
        campaign.setEndAt(LocalDateTime.now(clock).minusDays(1));
        when(campaignRepository.findAllActiveDefaults()).thenReturn(List.of(campaign));
        assertThrows(DefaultCampaignNotFoundException.class, () -> resolver.resolveActiveGlobalDefault(null));
    }

    @Test
    void resolveGlobalDefault_inactiveStatus_collectableCheckFails() {
        Campaign campaign = activeCampaign(LINE_ID);
        campaign.setStatus(CampaignStatus.ARCHIVED);
        when(campaignRepository.findAllActiveDefaults()).thenReturn(List.of(campaign));
        // findAllActiveDefaults filters ACTIVE in repo; simulate edge case if returned
        assertThrows(CampaignNotActiveException.class, () -> resolver.resolveActiveGlobalDefault(null));
    }

    private Campaign activeCampaign(UUID lineId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Campaign c = Campaign.builder()
                .lineId(lineId)
                .isDefault(true)
                .status(CampaignStatus.ACTIVE)
                .campaignType(CampaignType.STANDARD)
                .code("DEF-" + lineId.toString().substring(0, 4))
                .name("C")
                .displayName("C")
                .priority(0)
                .startAt(now.minusDays(1))
                .endAt(now.plusDays(1))
                .build();
        c.setId(UUID.randomUUID());
        return c;
    }
}
