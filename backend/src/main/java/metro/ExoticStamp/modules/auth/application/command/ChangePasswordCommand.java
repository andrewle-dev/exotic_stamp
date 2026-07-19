package metro.ExoticStamp.modules.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordCommand {
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;
    private String ipAddress;
}
