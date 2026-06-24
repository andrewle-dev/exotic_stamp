package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ImportVouchersRequest {

    @NotNull
    private UUID milestoneId;

    @NotEmpty
    private List<@NotNull String> codes;

    private LocalDateTime expiresAt;
}
