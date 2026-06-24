package metro.ExoticStamp.modules.collection.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "Collection progress for a line")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    @Schema(description = "Metro line id")
    private UUID lineId;
    @Schema(description = "Distinct stations collected for the campaign")
    private long collected;
    @Schema(description = "Active stations on the line")
    private long total;
    @Schema(description = "Rounded percentage 0–100")
    private int percentage;
}

