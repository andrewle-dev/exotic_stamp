package metro.ExoticStamp.modules.rbac.application;

import metro.ExoticStamp.modules.rbac.application.mapper.RoleAppMapper;
import metro.ExoticStamp.modules.rbac.application.view.PermissionView;
import metro.ExoticStamp.modules.rbac.application.view.RoleView;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleQueryService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional(readOnly = true)
    public RoleView getRoleById(UUID roleId) {
        return roleRepository.findById(roleId)
                .map(RoleAppMapper::toRoleView)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    @Transactional(readOnly = true)
    public List<RoleView> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleAppMapper::toRoleView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleView> getRolesByUserId(UUID userId) {
        return userRoleRepository.findAllByUserId(userId)
                .stream()
                .map(ur -> RoleAppMapper.toRoleView(ur.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PermissionView> getPermissionsByRoleId(UUID roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        return role.getRolePermissions()
                .stream()
                .map(rp -> RoleAppMapper.toPermissionView(rp.getPermission()))
                .toList();
    }

    /** Used by auth (JWT claims) and security filter (authorities). */
    @Transactional(readOnly = true)
    public List<String> getRoleNamesByUserId(UUID userId) {
        return userRoleRepository.findAllByUserId(userId)
                .stream()
                .map(ur -> ur.getRole().getRole())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getPermissionCodesByUserId(UUID userId) {
        return userRoleRepository.findDistinctPermissionCodesByUserId(userId);
    }
}
