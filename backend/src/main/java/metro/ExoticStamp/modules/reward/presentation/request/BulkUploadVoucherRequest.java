package metro.ExoticStamp.modules.reward.presentation.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkUploadVoucherRequest {

    @NotEmpty
    private List<@jakarta.validation.constraints.NotBlank String> codes;
}
