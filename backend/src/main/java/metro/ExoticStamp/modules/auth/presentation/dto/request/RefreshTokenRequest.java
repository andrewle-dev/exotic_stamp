package metro.ExoticStamp.modules.auth.presentation.dto.request;

/**
 * Optional body for native refresh. Web uses the HttpOnly cookie instead.
 */
public class RefreshTokenRequest {

    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
