package metro.ExoticStamp.modules.rbac.application.mapper;

import metro.ExoticStamp.modules.rbac.application.view.PermissionView;
import metro.ExoticStamp.modules.rbac.application.view.RoleView;
import metro.ExoticStamp.modules.rbac.domain.model.Permission;
import metro.ExoticStamp.modules.rbac.domain.model.Role;

public final class RoleAppMapper {

    private RoleAppMapper() {}

    public static RoleView toRoleView(Role role) {
        return new RoleView(
                role.getId(),
                role.getRole(),
                role.getDescription(),
                role.getStatus() == null ? null : role.getStatus().name(),
                role.isSystemRole()
        );
    }

    public static PermissionView toPermissionView(Permission permission) {
        return new PermissionView(
                permission.getId(),
                permission.getPermission(),
                permission.getDescription()
        );
    }
}
