package metro.ExoticStamp.modules.auth.infrastructure.jwt;

import java.util.UUID;

/**
 * Validated access-token claims after signature and type checks.
 */
public record ParsedAccessToken(UUID userId, String jti, long tokenVersion) {}
