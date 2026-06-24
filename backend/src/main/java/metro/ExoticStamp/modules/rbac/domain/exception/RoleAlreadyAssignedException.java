package metro.ExoticStamp.modules.rbac.domain.exception;

import java.util.UUID;

public class RoleAlreadyAssignedException extends RuntimeException {

    public RoleAlreadyAssignedException(UUID userId, String roleName) {
        super("User " + userId + " already has role: " + roleName);
    }
}