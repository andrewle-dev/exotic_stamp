package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.infrastructure.bootstrap.CollectionBootstrapper;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionBootstrapperTest {

    private static final UUID LINE_ID_1 = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID LINE_ID_2 = UUID.fromString("00000000-0000-0000-0000-000000000011");

    @Mock private LineReadPort lineReadPort;
    @Mock private CampaignRepository campaignRepository;

    private CollectionBootstrapper bootstrapper;

    @BeforeEach
    void setUp() {
        bootstrapper = new CollectionBootstrapper(lineReadPort, campaignRepository);
    }

    @Test
    void bootstrapsMissingDefaults_idempotently() {
        when(lineReadPort.getAllActiveLines()).thenReturn(List.of(
                MetroLineView.builder().id(LINE_ID_1).code("L1").name("Line1").active(true).build(),
                MetroLineView.builder().id(LINE_ID_2).code("L2").name("Line2").active(true).build()
        ));
        when(campaignRepository.existsDefaultByLineId(LINE_ID_1)).thenReturn(false);
        when(campaignRepository.existsDefaultByLineId(LINE_ID_2)).thenReturn(true);

        bootstrapper.run(new DefaultApplicationArguments(new String[]{}));

        verify(campaignRepository, times(1)).save(any());
    }
}

