package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderStationsRequest {
    @NotNull
    private UUID lineId;
    @NotNull
    private List<UUID> orderedIds;
}
