package metro.ExoticStamp.modules.rbac.domain.repository;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleName;

import java.util.List;
import java.util.Optional;

// Domain interface — không import Spring/JPA
public interface RoleRepository {
    Optional<Role> findById(UUID id);

    Optional<Role> findByRoleName(RoleName roleName);

    Optional<Role> findByRoleCode(String normalizedRoleCode);

    Optional<Role> findByIdWithPermissions(UUID id);

    List<Role> findAll();

    Role save(Role role);
}


