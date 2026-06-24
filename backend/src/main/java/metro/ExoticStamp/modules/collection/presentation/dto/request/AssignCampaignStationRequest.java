package metro.ExoticStamp.modules.collection.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignCampaignStationRequest {

    @NotNull
    private UUID stationId;
}
