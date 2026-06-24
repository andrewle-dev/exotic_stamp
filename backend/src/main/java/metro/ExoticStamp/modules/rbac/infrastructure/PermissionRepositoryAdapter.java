package metro.ExoticStamp.modules.rbac.infrastructure;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.Permission;
import metro.ExoticStamp.modules.rbac.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final JpaPermissionRepository jpa;

    @Override
    public Optional<Permission> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Permission> findByPermissionCode(String code) {
        return jpa.findByPermission(code);
    }

    @Override
    public boolean existsByPermissionCode(String code) {
        return jpa.existsByPermission(code);
    }

    @Override
    public List<Permission> findAllOrderByPermission() {
        return jpa.findAll(Sort.by("permission").ascending());
    }

    @Override
    public Permission save(Permission permission) {
        return jpa.save(permission);
    }
}



