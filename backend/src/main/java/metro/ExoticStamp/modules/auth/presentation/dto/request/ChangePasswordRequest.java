package metro.ExoticStamp.modules.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import metro.ExoticStamp.modules.auth.domain.PasswordPolicy;

@Schema(description = "Authenticated user change-password request")
public class ChangePasswordRequest {

    @NotBlank
    @Size(max = PasswordPolicy.MAX_LENGTH)
    @Schema(example = "CurrentPass1!", description = "User's current password")
    private String currentPassword;

    @NotBlank
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH)
    @Schema(example = "NewSecurePass2!", description = "New password (8-50 characters)")
    private String newPassword;

    @NotBlank
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH)
    @Schema(example = "NewSecurePass2!", description = "Must match newPassword")
    private String confirmNewPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
