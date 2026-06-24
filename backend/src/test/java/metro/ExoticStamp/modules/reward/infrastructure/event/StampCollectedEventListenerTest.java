package metro.ExoticStamp.modules.reward.infrastructure.event;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.reward.application.port.RewardStampCollectedDedupPort;
import metro.ExoticStamp.modules.reward.application.service.RewardEvaluationService;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampCollectedEventListenerTest {

    @Mock
    private RewardStampCollectedDedupPort dedupPort;
    @Mock
    private RewardEvaluationService rewardEvaluationService;
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private Counter counter;
    @Mock
    private RewardProperties rewardProperties;

    private StampCollectedEventListener listener;

    @BeforeEach
    void setUp() {
        lenient().when(rewardProperties.getStampCollectedEventMaxAttempts()).thenReturn(3);
        lenient().when(rewardProperties.getStampCollectedEventRetryBackoff()).thenReturn(Duration.ZERO);
        listener = new StampCollectedEventListener(dedupPort, rewardEvaluationService, meterRegistry, rewardProperties);
    }

    @Test
    void duplicate_skipsProcessing() {
        UUID eventId = UUID.randomUUID();
        when(dedupPort.isProcessed(eventId)).thenReturn(true);
        StampCollectedEvent event = new StampCollectedEvent(
                this, eventId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), CollectMethod.NFC
        );
        listener.onStampCollected(event);
        verify(rewardEvaluationService, never()).handleStampCollected(any(), any());
    }

    @Test
    void first_run_invokesEvaluation() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        when(dedupPort.isProcessed(eventId)).thenReturn(false);
        when(dedupPort.acquireProcessingLock(eventId)).thenReturn(true);
        when(meterRegistry.counter(eq("reward.milestone.checked"), eq("lineId"), eq(lineId.toString())))
                .thenReturn(counter);
        StampCollectedEvent event = new StampCollectedEvent(
                this, eventId, UUID.randomUUID(), userId, UUID.randomUUID(), lineId, campaignId,
                LocalDateTime.now(), CollectMethod.QR
        );
        listener.onStampCollected(event);
        verify(rewardEvaluationService).handleStampCollected(userId, campaignId);
        verify(dedupPort).markProcessed(eventId);
        verify(dedupPort).releaseProcessingLock(eventId);
        verify(counter).increment();
    }

    @Test
    void handlerException_swallowed() {
        UUID eventId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(dedupPort.isProcessed(eventId)).thenReturn(false);
        when(dedupPort.acquireProcessingLock(eventId)).thenReturn(true);
        when(meterRegistry.counter(eq("reward.milestone.checked"), eq("lineId"), eq(lineId.toString())))
                .thenReturn(counter);
        doThrow(new RuntimeException("boom")).when(rewardEvaluationService).handleStampCollected(any(), any());
        StampCollectedEvent event = new StampCollectedEvent(
                this, eventId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                lineId, UUID.randomUUID(), LocalDateTime.now(), CollectMethod.NFC
        );
        listener.onStampCollected(event);
        verify(rewardEvaluationService, times(3)).handleStampCollected(any(), any());
        verify(dedupPort, never()).markProcessed(eventId);
        verify(dedupPort).releaseProcessingLock(eventId);
    }
}
