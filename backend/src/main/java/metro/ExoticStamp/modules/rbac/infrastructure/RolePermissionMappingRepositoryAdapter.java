package metro.ExoticStamp.modules.rbac.infrastructure;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.RolePermission;
import metro.ExoticStamp.modules.rbac.domain.repository.RolePermissionMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RolePermissionMappingRepositoryAdapter implements RolePermissionMappingRepository {

    private final JpaRolePermissionRepository jpa;

    @Override
    public RolePermission save(RolePermission mapping) {
        return jpa.save(mapping);
    }

    @Override
    public boolean existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId) {
        return jpa.existsByRole_IdAndPermission_Id(roleId, permissionId);
    }

    @Override
    public void deleteByRoleIdAndPermissionId(UUID roleId, UUID permissionId) {
        jpa.deleteByRole_IdAndPermission_Id(roleId, permissionId);
    }
}



