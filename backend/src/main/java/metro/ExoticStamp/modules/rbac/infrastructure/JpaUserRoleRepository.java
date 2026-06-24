package metro.ExoticStamp.modules.rbac.infrastructure;

import metro.ExoticStamp.modules.rbac.domain.model.UserRole;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaUserRoleRepository extends JpaRepository<UserRole, UUID> {

    @EntityGraph(attributePaths = "role")
    List<UserRole> findAllByUserId(UUID userId);

    boolean existsByUserIdAndRoleId(UUID userId, UUID roleId);

    void deleteByUserIdAndRoleId(UUID userId, UUID roleId);

    @Query("""
            SELECT COUNT(DISTINCT ur.userId) FROM UserRole ur
            JOIN ur.role r
            JOIN User u ON u.id = ur.userId
            WHERE r.role = :adminCode AND u.status = :active
            """)
    long countActiveUsersWithRoleCode(
            @Param("adminCode") String adminCode,
            @Param("active") UserStatus active
    );

    @Query("""
            SELECT DISTINCT p.permission FROM UserRole ur
            JOIN ur.role r
            JOIN r.rolePermissions rp
            JOIN rp.permission p
            WHERE ur.userId = :userId
            """)
    List<String> findDistinctPermissionCodesByUserId(@Param("userId") UUID userId);
}
