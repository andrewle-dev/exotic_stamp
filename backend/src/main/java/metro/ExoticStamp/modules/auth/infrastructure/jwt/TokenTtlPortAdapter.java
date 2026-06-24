package metro.ExoticStamp.modules.auth.infrastructure.jwt;

import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenTtlPortAdapter implements TokenTtlPort {

    private final JwtProperties jwtProperties;

    @Override
    public Duration getAccessTokenTtl() {
        return jwtProperties.getAccessTokenTtl();
    }

    @Override
    public Duration getRefreshTokenTtl() {
        return jwtProperties.getRefreshTokenTtl();
    }
}
