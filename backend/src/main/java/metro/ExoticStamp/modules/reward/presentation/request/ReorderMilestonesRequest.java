package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderMilestonesRequest {
    @NotNull
    private UUID campaignId;
    @NotNull
    private List<UUID> orderedIds;
}
