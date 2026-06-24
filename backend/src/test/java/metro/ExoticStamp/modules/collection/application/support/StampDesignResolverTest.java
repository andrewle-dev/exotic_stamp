package metro.ExoticStamp.modules.collection.application.support;

import metro.ExoticStamp.modules.collection.domain.exception.StampDesignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampDesignResolverTest {

    @Mock private StampDesignRepository stampDesignRepository;
    @InjectMocks private StampDesignResolver resolver;

    @Test
    void resolveActive_found() {
        UUID campaignId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        StampDesign design = StampDesign.builder().campaignId(campaignId).stationId(stationId).name("S")
                .status(StampDesignStatus.ACTIVE).rarity(StampRarity.COMMON).sortOrder(0).isLimited(false).build();
        when(stampDesignRepository.findActiveByCampaignIdAndStationId(campaignId, stationId))
                .thenReturn(Optional.of(design));
        assertEquals(design, resolver.resolveActive(campaignId, stationId));
    }

    @Test
    void resolveActive_notFound() {
        UUID campaignId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        when(stampDesignRepository.findActiveByCampaignIdAndStationId(campaignId, stationId))
                .thenReturn(Optional.empty());
        assertThrows(StampDesignNotFoundException.class, () -> resolver.resolveActive(campaignId, stationId));
    }
}
