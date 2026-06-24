package metro.ExoticStamp.modules.auth.infrastructure.jwt;

/**
 * Newly minted access JWT plus its {@code jti} for Redis bookkeeping (device slot, denylist).
 */
public record IssuedAccessToken(String token, String jti) {}
