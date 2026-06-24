package metro.ExoticStamp.modules.rbac.domain.exception;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.RoleName;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(RoleName roleName) {
        super("Role not found with: " + roleName.name());
    }

    public RoleNotFoundException(UUID id) {
        super("Role not found with id: " + id);
    }

    public RoleNotFoundException(String roleCode) {
        super("Role not found with code: " + roleCode);
    }

}



