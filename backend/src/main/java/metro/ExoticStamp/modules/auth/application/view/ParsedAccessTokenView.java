package metro.ExoticStamp.modules.auth.application.view;

import java.util.UUID;

public record ParsedAccessTokenView(UUID userId, String jti, long tokenVersion) {
}
