package metro.ExoticStamp.modules.auth.application.view;

public record AuthView(
        String accessToken,
        String refreshToken,
        AuthUserView user
) {
}
