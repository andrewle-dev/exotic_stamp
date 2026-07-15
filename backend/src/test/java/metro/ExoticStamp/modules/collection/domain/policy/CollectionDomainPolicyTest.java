package metro.ExoticStamp.modules.collection.domain.policy;

import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotActiveException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignStationNotEligibleException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsAccuracyTooLowException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsOutOfRangeException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsRequiredException;
import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyKeyConflictException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CollectionDomainPolicyTest {

    @Test
    void duplicate_rejectsWhenExists() {
        UUID stationId = UUID.randomUUID();
        assertThrows(StampAlreadyCollectedException.class,
                () -> CollectionDuplicatePolicy.assertNotAlreadyCollected(true, stationId));
        assertDoesNotThrow(() -> CollectionDuplicatePolicy.assertNotAlreadyCollected(false, stationId));
    }

    @Test
    void idempotency_rejectsCrossUserReplay() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        assertThrows(IdempotencyKeyConflictException.class,
                () -> CollectionDuplicatePolicy.assertReplayBelongsToUser(owner, other));
        assertDoesNotThrow(() -> CollectionDuplicatePolicy.assertReplayBelongsToUser(owner, owner));
    }

    @Test
    void eligibility_rejectsMissingCampaignStation() {
        UUID campaignId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        assertThrows(CampaignStationNotEligibleException.class,
                () -> CollectionEligibilityPolicy.assertCampaignStationEligible(false, campaignId, stationId));
        assertDoesNotThrow(
                () -> CollectionEligibilityPolicy.assertCampaignStationEligible(true, campaignId, stationId));
    }

    @Test
    void eligibility_rejectsInactiveCampaign() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0);
        Campaign inactive = Campaign.builder()
                .code("C1")
                .name("n")
                .displayName("n")
                .campaignType(CampaignType.STANDARD)
                .status(CampaignStatus.DRAFT)
                .startAt(now.minusDays(1))
                .endAt(now.plusDays(1))
                .isDefault(true)
                .build();
        inactive.setId(UUID.randomUUID());
        assertThrows(CampaignNotActiveException.class,
                () -> CollectionEligibilityPolicy.assertCampaignCollectable(inactive, now));
    }

    @Test
    void gps_rejectsMissingCoords() {
        CollectionGpsPolicy.StationLocation station = new CollectionGpsPolicy.StationLocation(
                BigDecimal.TEN, BigDecimal.TEN, 150);
        assertThrows(GpsRequiredException.class,
                () -> CollectionGpsPolicy.validate(null, BigDecimal.ONE, BigDecimal.TEN, station, 6_371_000));
    }

    @Test
    void gps_rejectsOutOfRange() {
        CollectionGpsPolicy.StationLocation station = new CollectionGpsPolicy.StationLocation(
                BigDecimal.valueOf(10), BigDecimal.valueOf(20), 50);
        assertThrows(GpsOutOfRangeException.class,
                () -> CollectionGpsPolicy.validate(
                        BigDecimal.valueOf(10.01), BigDecimal.valueOf(20.01), BigDecimal.valueOf(30),
                        station, 6_371_000));
    }

    @Test
    void gps_rejectsPoorAccuracy() {
        CollectionGpsPolicy.StationLocation station = new CollectionGpsPolicy.StationLocation(
                BigDecimal.TEN, BigDecimal.TEN, 150);
        assertThrows(GpsAccuracyTooLowException.class,
                () -> CollectionGpsPolicy.validate(
                        BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(250), station, 6_371_000));
    }

    @Test
    void gps_zoneRadiusFallback() {
        assertEquals(150.0, CollectionGpsPolicy.resolveZoneRadius(5));
        assertEquals(150.0, CollectionGpsPolicy.resolveZoneRadius(5000));
        assertEquals(80.0, CollectionGpsPolicy.resolveZoneRadius(80));
    }
}
