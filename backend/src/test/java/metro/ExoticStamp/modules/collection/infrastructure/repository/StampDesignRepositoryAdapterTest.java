package metro.ExoticStamp.modules.collection.infrastructure.repository;

import metro.ExoticStamp.modules.collection.domain.exception.DuplicateActiveStampDesignException;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampDesignRepositoryAdapterTest {

    @Mock private JpaStampDesignRepository jpa;

    @InjectMocks private StampDesignRepositoryAdapter adapter;

    @Test
    void findAllByIdIn_emptyReturnsEmptyList() {
        assertTrue(adapter.findAllByIdIn(null).isEmpty());
        assertTrue(adapter.findAllByIdIn(List.of()).isEmpty());
    }

    @Test
    void findActiveByCampaignIdAndStationIdIn_emptyReturnsEmptyList() {
        assertTrue(adapter.findActiveByCampaignIdAndStationIdIn(UUID.randomUUID(), null).isEmpty());
        assertTrue(adapter.findActiveByCampaignIdAndStationIdIn(UUID.randomUUID(), List.of()).isEmpty());
    }

    @Test
    void save_activeDesignViolation_mapsDuplicateException() {
        StampDesign design = StampDesign.builder()
                .campaignId(UUID.randomUUID())
                .stationId(UUID.randomUUID())
                .name("S")
                .imageUrl("https://cdn/x.png")
                .status(StampDesignStatus.ACTIVE)
                .rarity(StampRarity.COMMON)
                .sortOrder(0)
                .isLimited(false)
                .build();
        when(jpa.save(design)).thenThrow(new DataIntegrityViolationException(
                "dup", new RuntimeException("uq_stamp_design_active_per_campaign_station violated")));

        assertThrows(DuplicateActiveStampDesignException.class, () -> adapter.save(design));
    }

    @Test
    void save_otherIntegrityViolation_rethrows() {
        StampDesign design = StampDesign.builder()
                .campaignId(UUID.randomUUID())
                .stationId(UUID.randomUUID())
                .name("S")
                .imageUrl("https://cdn/x.png")
                .status(StampDesignStatus.ACTIVE)
                .rarity(StampRarity.COMMON)
                .sortOrder(0)
                .isLimited(false)
                .build();
        DataIntegrityViolationException ex = new DataIntegrityViolationException("other constraint");
        when(jpa.save(design)).thenThrow(ex);

        assertEquals(ex, assertThrows(DataIntegrityViolationException.class, () -> adapter.save(design)));
    }
}
