package metro.ExoticStamp.modules.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCommand {
    private UUID id;
    private String    firstname;
    private String    lastname;
    private String    bio;
    private String    avatarUrl;
    private Boolean   gender;  // Boolean nullable
    private LocalDate dob;
}
