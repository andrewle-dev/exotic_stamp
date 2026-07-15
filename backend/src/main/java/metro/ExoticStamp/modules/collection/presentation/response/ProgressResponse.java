package metro.ExoticStamp.modules.collection.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "Collection progress for the active default campaign")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {
    @Schema(description = "Effective metro line id (campaign line when set, else request lineId)")
    private UUID lineId;
    @Schema(description = "Distinct stations collected by the user in the default campaign")
    private long collected;
    @Schema(description = "ACTIVE stamp designs eligible in the same default campaign")
    private long total;
    @Schema(description = "Rounded percentage 0–100 (capped when collected exceeds total)")
    private int percentage;
}

