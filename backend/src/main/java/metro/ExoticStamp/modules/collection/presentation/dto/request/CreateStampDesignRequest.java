package metro.ExoticStamp.modules.collection.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import metro.ExoticStamp.modules.collection.presentation.dto.StampDesignStatusApi;
import metro.ExoticStamp.modules.collection.presentation.dto.StampRarityApi;

import java.util.UUID;

@Data
public class CreateStampDesignRequest {

    @NotNull
    private UUID campaignId;

    @NotNull
    private UUID stationId;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String imageUrl;

    private String previewImageUrl;

    private StampRarityApi rarity;

    private StampDesignStatusApi status;

    private Integer sortOrder;
}
