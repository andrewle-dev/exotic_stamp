package metro.ExoticStamp.modules.collection.application.support;

import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyConflictException;
import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyKeyConflictException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionPolicyServiceTest {

    private static final UUID U1 = UUID.randomUUID();
    private static final UUID U2 = UUID.randomUUID();
    private static final UUID STATION = UUID.randomUUID();
    private static final UUID CAMPAIGN = UUID.randomUUID();

    @Mock private UserStampRepository userStampRepository;
    @Mock private CollectionRuntimeAuditHelper auditHelper;

    private CollectionPolicyService service;

    @BeforeEach
    void setUp() {
        service = new CollectionPolicyService(userStampRepository, auditHelper);
    }

    @Test
    void assertCollectAllowed_duplicateThrowsAndAudits() {
        when(userStampRepository.existsByUserIdAndStationIdAndCampaignId(U1, STATION, CAMPAIGN)).thenReturn(true);
        assertThrows(StampAlreadyCollectedException.class, () -> service.assertCollectAllowed(U1, STATION, CAMPAIGN));
        verify(auditHelper).scheduleDuplicateAttempt(U1, STATION, CAMPAIGN);
    }

    @Test
    void resolveIdempotentReplay_sameFingerprint_replays() {
        String fp = CollectIdempotencyFingerprint.compute(U1, STATION, CAMPAIGN, "NFC");
        UserStamp stamp = stamp(U1, STATION, CAMPAIGN, fp);
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(U1, "key"))
                .thenReturn(Optional.of(stamp));
        assertTrue(service.resolveIdempotentReplay("key", U1, fp, STATION, CAMPAIGN).isPresent());
    }

    @Test
    void resolveIdempotentReplay_differentFingerprint_conflicts() {
        String stored = CollectIdempotencyFingerprint.compute(U1, STATION, CAMPAIGN, "NFC");
        String other = CollectIdempotencyFingerprint.compute(U1, UUID.randomUUID(), CAMPAIGN, "NFC");
        UserStamp stamp = stamp(U1, STATION, CAMPAIGN, stored);
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(U1, "key"))
                .thenReturn(Optional.of(stamp));
        assertThrows(IdempotencyConflictException.class,
                () -> service.resolveIdempotentReplay("key", U1, other, STATION, CAMPAIGN));
    }

    @Test
    void resolveIdempotentReplay_legacyNullFingerprint_sameStationCampaign_replays() {
        UserStamp stamp = stamp(U1, STATION, CAMPAIGN, null);
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(U1, "key"))
                .thenReturn(Optional.of(stamp));
        String fp = CollectIdempotencyFingerprint.compute(U1, STATION, CAMPAIGN, "NFC");
        assertTrue(service.resolveIdempotentReplay("key", U1, fp, STATION, CAMPAIGN).isPresent());
    }

    @Test
    void resolveIdempotentReplay_legacyNullFingerprint_differentStation_conflicts() {
        UserStamp stamp = stamp(U1, STATION, CAMPAIGN, null);
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(U1, "key"))
                .thenReturn(Optional.of(stamp));
        UUID otherStation = UUID.randomUUID();
        String fp = CollectIdempotencyFingerprint.compute(U1, otherStation, CAMPAIGN, "NFC");
        assertThrows(IdempotencyConflictException.class,
                () -> service.resolveIdempotentReplay("key", U1, fp, otherStation, CAMPAIGN));
    }

    @Test
    void resolveIdempotentReplay_crossUserConflict() {
        UserStamp stamp = stamp(U2, STATION, CAMPAIGN, "fp");
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(U1, "key"))
                .thenReturn(Optional.of(stamp));
        assertThrows(IdempotencyKeyConflictException.class,
                () -> service.resolveIdempotentReplay("key", U1, "fp", STATION, CAMPAIGN));
    }

    @Test
    void resolveIdempotentReplay_blankKey_returnsEmpty() {
        assertTrue(service.resolveIdempotentReplay(null, U1, "fp", STATION, CAMPAIGN).isEmpty());
        assertTrue(service.resolveIdempotentReplay("  ", U1, "fp", STATION, CAMPAIGN).isEmpty());
    }

    @Test
    void resolveIdempotentReplay_legacyNullFingerprint_differentCampaign_conflicts() {
        UserStamp stamp = stamp(U1, STATION, CAMPAIGN, null);
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(U1, "key"))
                .thenReturn(Optional.of(stamp));
        UUID otherCampaign = UUID.randomUUID();
        String fp = CollectIdempotencyFingerprint.compute(U1, STATION, otherCampaign, "NFC");
        assertThrows(IdempotencyConflictException.class,
                () -> service.resolveIdempotentReplay("key", U1, fp, STATION, otherCampaign));
    }

    @Test
    void assertLogicalMatch_storedFingerprintMismatch_conflicts() {
        UserStamp stamp = stamp(U1, STATION, CAMPAIGN, "stored-fp");
        String other = CollectIdempotencyFingerprint.compute(U1, STATION, CAMPAIGN, "QR_STATIC");
        assertThrows(IdempotencyConflictException.class,
                () -> service.assertLogicalMatch(stamp, other, STATION, CAMPAIGN));
    }

    @Test
    void resolveIdempotentReplay_softLookup_returnsStampForUser() {
        UserStamp stamp = stamp(U1, STATION, CAMPAIGN, "fp");
        when(userStampRepository.findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(U1, "key"))
                .thenReturn(Optional.of(stamp));
        assertTrue(service.resolveIdempotentReplay("key", U1).isPresent());
    }

    private static UserStamp stamp(UUID userId, UUID stationId, UUID campaignId, String fingerprint) {
        return UserStamp.builder()
                .userId(userId).stationId(stationId).campaignId(campaignId).stampDesignId(UUID.randomUUID())
                .collectedAt(LocalDateTime.now()).gpsVerified(true).collectMethod(CollectMethod.NFC)
                .deviceFingerprint("fp").idempotencyKey("key").idempotencyFingerprint(fingerprint)
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN").build();
    }
}
