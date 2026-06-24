package metro.ExoticStamp.modules.metro.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotateQrTokenRequest {

    @NotBlank
    @Size(max = 100)
    private String qrCodeToken;
}
