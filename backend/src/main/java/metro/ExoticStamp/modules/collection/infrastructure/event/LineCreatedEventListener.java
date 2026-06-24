package metro.ExoticStamp.modules.collection.infrastructure.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.domain.factory.DefaultCampaignFactory;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroLineView;
import metro.ExoticStamp.modules.metro.domain.event.LineCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LineCreatedEventListener {

    private final CampaignRepository campaignRepository;
    private final LineReadPort lineReadPort;

    @Async
    @EventListener
    public void onLineCreated(LineCreatedEvent event) {
        UUID lineId = event.lineId();
        if (lineId == null) {
            return;
        }

        if (campaignRepository.existsDefaultByLineId(lineId)) {
            return;
        }

        MetroLineView line = lineReadPort.getLineById(lineId);
        Campaign campaign = DefaultCampaignFactory.createDefaultForLine(lineId, line.name(), line.code());
        campaignRepository.save(campaign);
        log.info("[Collection] Default campaign created for lineId={}", lineId);
    }
}
