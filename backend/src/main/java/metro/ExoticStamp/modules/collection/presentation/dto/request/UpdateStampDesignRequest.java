package metro.ExoticStamp.modules.collection.presentation.dto.request;

import lombok.Data;
import metro.ExoticStamp.modules.collection.presentation.dto.StampDesignStatusApi;
import metro.ExoticStamp.modules.collection.presentation.dto.StampRarityApi;

import java.util.UUID;

@Data
public class UpdateStampDesignRequest {

    private UUID campaignId;
    private UUID stationId;
    private String name;
    private String description;
    private String imageUrl;
    private String previewImageUrl;
    private StampRarityApi rarity;
    private StampDesignStatusApi status;
    private Integer sortOrder;
}
