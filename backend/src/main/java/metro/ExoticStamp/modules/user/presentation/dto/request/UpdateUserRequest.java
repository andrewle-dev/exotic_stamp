package metro.ExoticStamp.modules.user.presentation.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 50)
    private String firstname;

    @Size(max = 50)
    private String lastname;

    @Size(max = 100)
    private String bio;

    private String  avatarUrl;

    private Boolean gender;  // nullable — phân biệt "không gửi" vs false

    @Past(message = "Date of birth must be in the past")
    private LocalDate dob;

    // username/email/phone/password → endpoint riêng
    // mỗi cái cần flow verify khác nhau (OTP, confirm password...)
}
