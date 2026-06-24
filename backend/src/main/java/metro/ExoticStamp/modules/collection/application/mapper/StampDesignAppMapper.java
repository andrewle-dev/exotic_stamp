package metro.ExoticStamp.modules.collection.application.mapper;

import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import org.springframework.stereotype.Component;

@Component
public class StampDesignAppMapper {
    public StampDesign copyForReturn(StampDesign stampDesign) {
        return stampDesign;
    }
}

