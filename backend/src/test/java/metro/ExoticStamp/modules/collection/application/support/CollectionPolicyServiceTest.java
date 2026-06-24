package metro.ExoticStamp.modules.collection.application.support;

import metro.ExoticStamp.modules.collection.config.CollectionProperties;
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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        CollectionProperties props = new CollectionProperties();
        props.setIdempotencyWindow(Duration.ofHours(1));
        Clock clock = Clock.fixed(Instant.parse("2025-06-01T12:00:00Z"), ZoneOffset.UTC);
        service = new CollectionPolicyService(userStampRepository, props, clock, auditHelper);
    }

    @Test
    void assertCollectAllowed_duplicateThrowsAndAudits() {
        when(userStampRepository.existsByUserIdAndStationIdAndCampaignId(U1, STATION, CAMPAIGN)).thenReturn(true);
        assertThrows(StampAlreadyCollectedException.class, () -> service.assertCollectAllowed(U1, STATION, CAMPAIGN));
        verify(auditHelper).scheduleDuplicateAttempt(U1, STATION, CAMPAIGN);
    }

    @Test
    void resolveIdempotentReplay_sameUser() {
        UserStamp stamp = UserStamp.builder()
                .userId(U1).stationId(STATION).campaignId(CAMPAIGN).stampDesignId(UUID.randomUUID())
                .collectedAt(LocalDateTime.now()).gpsVerified(true).collectMethod(CollectMethod.NFC)
                .deviceFingerprint("fp").idempotencyKey("key").collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN").build();
        when(userStampRepository.findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(anyString(), any()))
                .thenReturn(Optional.of(stamp));
        assertTrue(service.resolveIdempotentReplay("key", U1).isPresent());
    }

    @Test
    void resolveIdempotentReplay_crossUserConflict() {
        UserStamp stamp = UserStamp.builder()
                .userId(U2).stationId(STATION).campaignId(CAMPAIGN).stampDesignId(UUID.randomUUID())
                .collectedAt(LocalDateTime.now()).gpsVerified(true).collectMethod(CollectMethod.NFC)
                .deviceFingerprint("fp").idempotencyKey("key").collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN").build();
        when(userStampRepository.findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(anyString(), any()))
                .thenReturn(Optional.of(stamp));
        assertThrows(IdempotencyKeyConflictException.class, () -> service.resolveIdempotentReplay("key", U1));
    }

    @Test
    void resolveIdempotentReplay_afterWindow_returnsEmpty() {
        when(userStampRepository.findFirstByIdempotencyKeyAndCollectedAtAfterOrderByCollectedAtDesc(anyString(), any()))
                .thenReturn(Optional.empty());
        assertTrue(service.resolveIdempotentReplay("expired-key", U1).isEmpty());
    }

    @Test
    void resolveIdempotentReplay_blankKey_returnsEmpty() {
        assertTrue(service.resolveIdempotentReplay(null, U1).isEmpty());
        assertTrue(service.resolveIdempotentReplay("  ", U1).isEmpty());
    }
}
