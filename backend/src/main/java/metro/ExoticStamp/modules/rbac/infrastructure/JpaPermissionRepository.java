package metro.ExoticStamp.modules.rbac.infrastructure;

import java.util.UUID;

import metro.ExoticStamp.modules.rbac.domain.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByPermission(String permission);

    boolean existsByPermission(String permission);
}



