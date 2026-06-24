package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdatePartnerRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String logoUrl;

    @Size(max = 100)
    private String contactEmail;

    private LocalDate contractStartDate;

    private LocalDate contractEndDate;
}
