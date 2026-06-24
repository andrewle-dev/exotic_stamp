package metro.ExoticStamp.modules.rbac.infrastructure;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaRolePermissionRepository extends JpaRepository<RolePermission, UUID> {

    boolean existsByRole_IdAndPermission_Id(UUID roleId, UUID permissionId);

    void deleteByRole_IdAndPermission_Id(UUID roleId, UUID permissionId);
}



