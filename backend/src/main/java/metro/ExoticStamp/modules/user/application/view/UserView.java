package metro.ExoticStamp.modules.user.application.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserView {
    private UUID id;
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String phoneNumber;
    private LocalDate dob;
    private boolean gender;
    private String bio;
    private String avatarUrl;
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
