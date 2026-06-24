package metro.ExoticStamp.modules.rbac.application;

import java.util.UUID;

import metro.ExoticStamp.config.RbacProperties;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.rbac.application.support.RbacAuditIp;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.rbac.domain.RbacAuditConstants;
import metro.ExoticStamp.modules.rbac.domain.RbacCodeNormalizer;
import metro.ExoticStamp.modules.rbac.domain.exception.DuplicateRbacMappingException;
import metro.ExoticStamp.modules.rbac.domain.exception.PermissionAlreadyExistsException;
import metro.ExoticStamp.modules.rbac.domain.exception.PermissionNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.model.Permission;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RolePermission;
import metro.ExoticStamp.modules.rbac.domain.repository.PermissionRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.RolePermissionMappingRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionCommandService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionMappingRepository rolePermissionMappingRepository;
    private final RbacProperties rbacProperties;
    private final AuditLogService auditLogService;
    private final RbacSecurityContextHelper securityContextHelper;

    @Transactional
    public Permission createPermission(String rawCode, String description) {
        String code = RbacCodeNormalizer.normalizeCode(rawCode, rbacProperties.getMaxPermissionCodeLength());
        if (permissionRepository.existsByPermissionCode(code)) {
            throw new PermissionAlreadyExistsException(code);
        }
        Permission saved = permissionRepository.save(Permission.builder()
                .permission(code)
                .description(description)
                .build());
        scheduleAudit(RbacAuditConstants.TABLE_PERMISSIONS, RbacAuditConstants.ACTION_PERMISSION_CREATE, null, saved);
        return saved;
    }

    @Transactional
    public void assignPermissionToRole(UUID roleId, String rawPermissionCode) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        String code = RbacCodeNormalizer.normalizeCode(rawPermissionCode, rbacProperties.getMaxPermissionCodeLength());
        Permission permission = permissionRepository.findByPermissionCode(code)
                .orElseThrow(() -> new PermissionNotFoundException(code));
        if (rolePermissionMappingRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            throw new DuplicateRbacMappingException("Permission already assigned to role");
        }
        rolePermissionMappingRepository.save(RolePermission.builder()
                .role(role)
                .permission(permission)
                .build());
        scheduleAudit(RbacAuditConstants.TABLE_ROLE_PERMISSIONS, RbacAuditConstants.ACTION_ROLE_PERMISSION_ASSIGN,
                null, roleId + ":" + permission.getId());
    }

    @Transactional
    public void revokePermissionFromRole(UUID roleId, UUID permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new PermissionNotFoundException(permissionId));
        if (!rolePermissionMappingRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId())) {
            return;
        }
        rolePermissionMappingRepository.deleteByRoleIdAndPermissionId(role.getId(), permission.getId());
        scheduleAudit(RbacAuditConstants.TABLE_ROLE_PERMISSIONS, RbacAuditConstants.ACTION_ROLE_PERMISSION_REVOKE,
                roleId + ":" + permission.getId(), null);
    }

    private void scheduleAudit(String table, String action, Object oldVal, Object newVal) {
        RbacTransactionCallbacks.afterCommit(() -> securityContextHelper.currentUserId().ifPresent(actorId ->
                auditLogService.log(actorId, table, action, oldVal, newVal, RbacAuditIp.UNKNOWN)));
    }
}



