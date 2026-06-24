package metro.ExoticStamp.modules.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResendVerificationCommand {
    private String email;
}
