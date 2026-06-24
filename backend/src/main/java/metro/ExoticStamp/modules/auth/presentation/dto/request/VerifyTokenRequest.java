package metro.ExoticStamp.modules.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public class VerifyTokenRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
