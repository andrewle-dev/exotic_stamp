package metro.ExoticStamp.modules.community.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplyReferralRequest {

    @NotBlank
    @Size(max = 20)
    private String code;
}
