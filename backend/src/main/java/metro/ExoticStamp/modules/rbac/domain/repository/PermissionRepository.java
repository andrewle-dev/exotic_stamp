package metro.ExoticStamp.modules.rbac.domain.repository;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository {

    Optional<Permission> findById(UUID id);

    Optional<Permission> findByPermissionCode(String code);

    boolean existsByPermissionCode(String code);

    List<Permission> findAllOrderByPermission();

    Permission save(Permission permission);
}



