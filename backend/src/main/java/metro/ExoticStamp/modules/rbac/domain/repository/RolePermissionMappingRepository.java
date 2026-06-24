package metro.ExoticStamp.modules.rbac.domain.repository;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.RolePermission;

public interface RolePermissionMappingRepository {

    RolePermission save(RolePermission mapping);

    boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId);
}



