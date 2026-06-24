package metro.ExoticStamp.modules.rbac.application;

import java.util.UUID;

import metro.ExoticStamp.config.RbacProperties;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.rbac.application.command.AssignRoleCommand;
import metro.ExoticStamp.modules.rbac.application.command.RevokeRoleCommand;
import metro.ExoticStamp.modules.rbac.application.support.RbacAuditIp;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.rbac.domain.RbacAuditConstants;
import metro.ExoticStamp.modules.rbac.domain.RbacCodeNormalizer;
import metro.ExoticStamp.modules.rbac.domain.exception.ImmutableRoleException;
import metro.ExoticStamp.modules.rbac.domain.exception.LastAdminProtectionException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleAlreadyAssignedException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleCodeAlreadyExistsException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleStatus;
import metro.ExoticStamp.modules.rbac.domain.model.UserRole;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RbacProperties rbacProperties;
    private final AuditLogService auditLogService;
    private final RbacSecurityContextHelper securityContextHelper;

    @Transactional
    public Role createRole(String rawRoleCode, String description) {
        String code = RbacCodeNormalizer.normalizeCode(rawRoleCode, rbacProperties.getMaxRoleCodeLength());
        if (roleRepository.findByRoleCode(code).isPresent()) {
            throw new RoleCodeAlreadyExistsException(code);
        }
        Role saved = roleRepository.save(Role.builder()
                .role(code)
                .description(description)
                .status(RoleStatus.ACTIVE)
                .systemRole(false)
                .build());
        scheduleAudit(RbacAuditConstants.TABLE_ROLES, RbacAuditConstants.ACTION_ROLE_CREATE, null, saved);
        return saved;
    }

    @Transactional
    public Role updateRole(UUID roleId, String rawNewCode, String description, String rawStatus) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
        Role before = snapshot(role);
        if (rawNewCode != null && !rawNewCode.isBlank()) {
            String newCode = RbacCodeNormalizer.normalizeCode(rawNewCode, rbacProperties.getMaxRoleCodeLength());
            if (!newCode.equals(role.getRole())) {
                if (role.isSystemRole() || isProtectedRoleCode(role.getRole())) {
                    throw new ImmutableRoleException("System role code cannot be renamed");
                }
                if (roleRepository.findByRoleCode(newCode).isPresent()) {
                    throw new RoleCodeAlreadyExistsException(newCode);
                }
                role.setRole(newCode);
            }
        }
        if (description != null) {
            role.setDescription(description);
        }
        if (rawStatus != null && !rawStatus.isBlank()) {
            RoleStatus next = parseStatus(rawStatus);
            if (next == RoleStatus.INACTIVE && (role.isSystemRole() || isProtectedRoleCode(role.getRole()))) {
                throw new ImmutableRoleException("Cannot deactivate a protected system role");
            }
            role.setStatus(next);
        }
        Role saved = roleRepository.save(role);
        scheduleAudit(RbacAuditConstants.TABLE_ROLES, RbacAuditConstants.ACTION_ROLE_UPDATE, before, saved);
        return saved;
    }

    @Transactional
    public void assignRole(AssignRoleCommand cmd) {
        String code = RbacCodeNormalizer.normalizeCode(cmd.getRoleName(), rbacProperties.getMaxRoleCodeLength());
        Role role = roleRepository.findByRoleCode(code)
                .orElseThrow(() -> new RoleNotFoundException(code));
        if (role.getStatus() != RoleStatus.ACTIVE) {
            throw new IllegalArgumentException("Role is not active: " + code);
        }
        boolean alreadyAssigned = userRoleRepository.existsByUserIdAndRoleId(cmd.getUserId(), role.getId());
        if (alreadyAssigned) {
            throw new RoleAlreadyAssignedException(cmd.getUserId(), code);
        }
        userRoleRepository.save(UserRole.builder()
                .userId(cmd.getUserId())
                .role(role)
                .build());
        scheduleAudit(RbacAuditConstants.TABLE_USER_ROLES, RbacAuditConstants.ACTION_USER_ROLE_ASSIGN,
                null, cmd.getUserId() + ":" + role.getId());
    }

    @Transactional
    public void revokeRole(RevokeRoleCommand cmd) {
        String code = RbacCodeNormalizer.normalizeCode(cmd.getRoleName(), rbacProperties.getMaxRoleCodeLength());
        Role role = roleRepository.findByRoleCode(code)
                .orElseThrow(() -> new RoleNotFoundException(code));
        if (!userRoleRepository.existsByUserIdAndRoleId(cmd.getUserId(), role.getId())) {
            return;
        }
        if (code.equals(rbacProperties.getAdminRoleCode())) {
            long activeAdmins = userRoleRepository.countActiveUsersWithRoleCode(rbacProperties.getAdminRoleCode());
            if (activeAdmins <= 1) {
                throw new LastAdminProtectionException();
            }
        }
        userRoleRepository.deleteByUserIdAndRoleId(cmd.getUserId(), role.getId());
        scheduleAudit(RbacAuditConstants.TABLE_USER_ROLES, RbacAuditConstants.ACTION_USER_ROLE_REVOKE,
                cmd.getUserId() + ":" + role.getId(), null);
    }

    private boolean isProtectedRoleCode(String roleCode) {
        String upper = roleCode.toUpperCase();
        return rbacProperties.getProtectedSystemRoleCodes().stream()
                .map(String::toUpperCase)
                .anyMatch(upper::equals);
    }

    private static RoleStatus parseStatus(String raw) {
        try {
            return RoleStatus.valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid role status: " + raw);
        }
    }

    private static Role snapshot(Role r) {
        return Role.builder()
                .id(r.getId())
                .role(r.getRole())
                .description(r.getDescription())
                .status(r.getStatus())
                .systemRole(r.isSystemRole())
                .version(r.getVersion())
                .build();
    }

    private void scheduleAudit(String table, String action, Object oldVal, Object newVal) {
        RbacTransactionCallbacks.afterCommit(() -> securityContextHelper.currentUserId().ifPresent(actorId ->
                auditLogService.log(actorId, table, action, oldVal, newVal, RbacAuditIp.UNKNOWN)));
    }
}



