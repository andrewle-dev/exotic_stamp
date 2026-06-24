package metro.ExoticStamp.modules.rbac.infrastructure;

import metro.ExoticStamp.modules.rbac.domain.model.UserRole;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRoleRepositoryAdapter implements UserRoleRepository {

    private final JpaUserRoleRepository jpa;

    @Override
    public UserRole save(UserRole ur) {
        return jpa.save(ur);
    }

    @Override
    public List<UserRole> findAllByUserId(UUID id) {
        return jpa.findAllByUserId(id);
    }

    @Override
    public boolean existsByUserIdAndRoleId(UUID uid, UUID rid) {
        return jpa.existsByUserIdAndRoleId(uid, rid);
    }

    @Override
    public void deleteByUserIdAndRoleId(UUID uid, UUID rid) {
        jpa.deleteByUserIdAndRoleId(uid, rid);
    }

    @Override
    public long countActiveUsersWithRoleCode(String normalizedRoleCode) {
        return jpa.countActiveUsersWithRoleCode(normalizedRoleCode, UserStatus.ACTIVE);
    }

    @Override
    public List<String> findDistinctPermissionCodesByUserId(UUID userId) {
        return jpa.findDistinctPermissionCodesByUserId(userId);
    }
}
