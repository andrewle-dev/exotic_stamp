package metro.ExoticStamp.modules.collection.presentation.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.modules.collection.application.view.CollectOutcomeStatus;

import java.time.LocalDateTime;

@Schema(description = "Read-only collect outcome for an idempotency key")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectStatusResponse {

    private CollectOutcomeStatus status;
    private CollectStampResponse.StampInfoResponse stamp;
    private CollectStampResponse.ScanInfoResponse scan;
    private ProgressResponse progress;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @Schema(description = "Present when status is FAILED")
    private String errorCode;
}
