package metro.ExoticStamp.modules.auth.application.port;

import metro.ExoticStamp.modules.auth.application.view.IssuedAccessTokenView;
import metro.ExoticStamp.modules.auth.application.view.ParsedAccessTokenView;
import metro.ExoticStamp.modules.user.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface AccessTokenPort {

    IssuedAccessTokenView issueAccessToken(User user, List<String> roles);

    ParsedAccessTokenView parseAccessToken(String token);

    String generateRefreshToken(UUID userId);

    String hashToken(String token);

    boolean isTokenValid(String token);

    UUID extractUserId(String token);
}
