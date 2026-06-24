package metro.ExoticStamp.modules.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordCommand {
    private String email;
    private String otp;
    private String newPassword;
}

