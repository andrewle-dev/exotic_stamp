package metro.ExoticStamp.modules.rbac.domain.repository;

import metro.ExoticStamp.modules.rbac.domain.model.UserRole;

import java.util.List;
import java.util.UUID;

// Domain interface — không import Spring/JPA
public interface UserRoleRepository {
    UserRole save(UserRole userRole);

    List<UserRole> findAllByUserId(UUID userId);

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    long countActiveUsersWithRoleCode(String normalizedRoleCode);

    List<String> findDistinctPermissionCodesByUserId(UUID userId);
}
