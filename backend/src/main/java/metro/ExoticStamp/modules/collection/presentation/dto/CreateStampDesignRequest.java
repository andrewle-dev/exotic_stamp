package metro.ExoticStamp.modules.collection.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateStampDesignRequest {

    private UUID stationId;

    private UUID campaignId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String artworkUrl;

    @Size(max = 255)
    private String animationUrl;

    @Size(max = 255)
    private String soundUrl;

    private boolean limited;

    private boolean active = true;
}
