package metro.ExoticStamp.modules.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCommand {
    private String    firstname;
    private String    lastname;
    private String    username;
    private String    email;
    private String    phoneNumber;
    private String    password;  // raw — encode trong CommandService
    private LocalDate dob;
    private boolean   gender;
    // Plain data object — không Spring, không logic
}