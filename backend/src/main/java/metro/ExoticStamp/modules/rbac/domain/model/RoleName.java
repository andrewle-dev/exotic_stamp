package metro.ExoticStamp.modules.rbac.domain.model;

public enum RoleName {
    ADMIN,        // full access + user management
    MANAGER,
    USER,         // basic access
    GUEST

    // Dùng trong @PreAuthorize:
    // @PreAuthorize("hasRole('ADMIN')")
    // @PreAuthorize("hasAnyRole('USER', 'GUEST')")
}