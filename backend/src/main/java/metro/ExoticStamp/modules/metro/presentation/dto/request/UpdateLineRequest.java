package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import metro.ExoticStamp.modules.metro.presentation.dto.MetroStatusApi;

@Data
public class UpdateLineRequest {
    @Size(max = 10)
    private String code;
    @Size(max = 100)
    private String name;
    @Size(max = 100)
    private String displayName;
    @Size(max = 500)
    private String description;
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "colorHex must be #RRGGBB")
    private String colorHex;
    private Integer sortOrder;
    private MetroStatusApi status;
}
