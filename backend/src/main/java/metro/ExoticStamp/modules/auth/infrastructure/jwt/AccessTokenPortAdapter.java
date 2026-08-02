package metro.ExoticStamp.modules.auth.infrastructure.jwt;

import metro.ExoticStamp.modules.auth.application.port.AccessTokenPort;
import metro.ExoticStamp.modules.auth.application.view.IssuedAccessTokenView;
import metro.ExoticStamp.modules.auth.application.view.ParsedAccessTokenView;
import metro.ExoticStamp.modules.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccessTokenPortAdapter implements AccessTokenPort {

    private final JwtProvider jwtProvider;

    @Override
    public IssuedAccessTokenView issueAccessToken(User user, List<String> roles) {
        IssuedAccessToken issued = jwtProvider.issueAccessToken(user, roles);
        return new IssuedAccessTokenView(issued.token(), issued.jti());
    }

    @Override
    public ParsedAccessTokenView parseAccessToken(String token) {
        ParsedAccessToken parsed = jwtProvider.parseAccessToken(token);
        return new ParsedAccessTokenView(parsed.userId(), parsed.jti(), parsed.tokenVersion());
    }

    @Override
    public String generateRefreshToken(UUID userId) {
        return jwtProvider.generateRefreshToken(userId);
    }

    @Override
    public String hashToken(String token) {
        return jwtProvider.hashToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return jwtProvider.isTokenValid(token);
    }

    @Override
    public UUID parseRefreshUserId(String token) {
        return jwtProvider.parseRefreshToken(token);
    }
}
