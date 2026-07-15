package metro.ExoticStamp.modules.collection.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderStampDesignsRequest {
    @NotNull
    private UUID campaignId;
    @NotNull
    private List<UUID> orderedIds;
}
