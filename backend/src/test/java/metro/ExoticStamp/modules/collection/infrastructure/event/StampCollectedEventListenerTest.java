package metro.ExoticStamp.modules.collection.infrastructure.event;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import metro.ExoticStamp.modules.collection.application.port.StampCollectedDedupPort;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StampCollectedEventListenerTest {

    private static final UUID STAMP_ID = UUID.fromString("00000000-0000-0000-0000-000000000040");

    @Mock
    private StampCollectedDedupPort stampCollectedDedupPort;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter counter;

    private StampCollectedEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new StampCollectedEventListener(stampCollectedDedupPort, meterRegistry);
    }

    @Test
    void duplicate_doesNotIncrementMeter() {
        UUID eventId = UUID.randomUUID();
        when(stampCollectedDedupPort.claimFirstProcessing(eventId)).thenReturn(false);
        StampCollectedEvent event = new StampCollectedEvent(
                this,
                eventId,
                STAMP_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                CollectMethod.NFC
        );
        listener.onStampCollected(event);
        verify(meterRegistry, never()).counter(eq("collection.stamp_collected"));
    }

    @Test
    void firstProcessing_incrementsMeter() {
        when(meterRegistry.counter("collection.stamp_collected")).thenReturn(counter);
        UUID eventId = UUID.randomUUID();
        when(stampCollectedDedupPort.claimFirstProcessing(eventId)).thenReturn(true);
        StampCollectedEvent event = new StampCollectedEvent(
                this,
                eventId,
                STAMP_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                CollectMethod.QR
        );
        listener.onStampCollected(event);
        verify(counter).increment();
    }

    @Test
    void handlerException_doesNotPropagate() {
        UUID eventId = UUID.randomUUID();
        when(stampCollectedDedupPort.claimFirstProcessing(eventId)).thenReturn(true);
        doThrow(new RuntimeException("meter down")).when(meterRegistry).counter(eq("collection.stamp_collected"));
        StampCollectedEvent event = new StampCollectedEvent(
                this,
                eventId,
                STAMP_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                CollectMethod.NFC
        );
        listener.onStampCollected(event);
    }
}
