package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.infrastructure.event.LineCreatedEventListener;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.domain.event.LineCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LineCreatedEventListenerTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock private CampaignRepository campaignRepository;
    @Mock private LineReadPort lineReadPort;

    private LineCreatedEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new LineCreatedEventListener(campaignRepository, lineReadPort);
    }

    @Test
    void createsDefaultCampaign_whenMissing() {
        when(campaignRepository.existsDefaultByLineId(LINE_ID)).thenReturn(false);
        when(lineReadPort.getLineById(LINE_ID))
                .thenReturn(MetroLineView.builder().id(LINE_ID).code("L1").name("Line 1").active(true).build());

        listener.onLineCreated(new LineCreatedEvent(LINE_ID));

        verify(campaignRepository).save(any());
    }

    @Test
    void idempotent_skip_whenAlreadyExists() {
        when(campaignRepository.existsDefaultByLineId(LINE_ID)).thenReturn(true);

        listener.onLineCreated(new LineCreatedEvent(LINE_ID));

        verify(campaignRepository, never()).save(any());
        verify(lineReadPort, never()).getLineById(any());
    }
}

