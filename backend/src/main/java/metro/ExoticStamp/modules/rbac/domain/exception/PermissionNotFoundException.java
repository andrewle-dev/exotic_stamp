package metro.ExoticStamp.modules.rbac.domain.exception;

import java.util.UUID;

public class PermissionNotFoundException extends RuntimeException {

    public PermissionNotFoundException(UUID id) {
        super("Permission not found with id: " + id);
    }

    public PermissionNotFoundException(String code) {
        super("Permission not found with code: " + code);
    }
}



