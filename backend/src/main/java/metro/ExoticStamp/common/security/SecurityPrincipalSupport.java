package metro.ExoticStamp.common.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public final class SecurityPrincipalSupport {

    private SecurityPrincipalSupport() {
    }

    public static UUID requireUserId(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Missing principal");
        }
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            UUID userId = authenticatedUser.getUserId();
            if (userId != null) {
                return userId;
            }
        }
        try {
            return UUID.fromString(principal.getUsername());
        } catch (Exception e) {
            throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName(), e);
        }
    }
}
