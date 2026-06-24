package metro.ExoticStamp.modules.community.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class RecordShareEventRequest {

    @NotBlank
    @Size(max = 20)
    private String platform;

    @NotBlank
    @Size(max = 30)
    private String shareType;

    private UUID targetId;

    private Map<String, Object> metadata;
}
