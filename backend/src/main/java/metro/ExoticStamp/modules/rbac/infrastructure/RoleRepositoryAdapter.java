package metro.ExoticStamp.modules.rbac.infrastructure;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleName;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final JpaRoleRepository jpa;

    @Override
    public Optional<Role> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Role> findByRoleName(RoleName roleName) {
        return jpa.findByRole(roleName.name());
    }

    @Override
    public Optional<Role> findByRoleCode(String normalizedRoleCode) {
        return jpa.findByRole(normalizedRoleCode);
    }

    @Override
    public Optional<Role> findByIdWithPermissions(UUID id) {
        return jpa.fetchByIdWithPermissions(id);
    }

    @Override
    public List<Role> findAll() {
        return jpa.findAll();
    }

    @Override
    public Role save(Role role) {
        return jpa.save(role);
    }
}


