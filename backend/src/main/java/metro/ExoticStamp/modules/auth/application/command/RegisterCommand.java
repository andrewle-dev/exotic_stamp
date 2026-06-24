package metro.ExoticStamp.modules.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCommand {
    private String firstname;
    private String lastname;
    private String username;

    private String email;
    private String phoneNumber;
    private String password;
}

