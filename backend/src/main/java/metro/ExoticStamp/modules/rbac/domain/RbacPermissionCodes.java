package metro.ExoticStamp.modules.rbac.domain;

/**
 * Well-known permission codes stored in {@code permissions.permission}.
 */
public final class RbacPermissionCodes {

    private RbacPermissionCodes() {}

    /** Grants access to enterprise RBAC admin APIs (create roles, permissions, mappings). */
    public static final String RBAC_ADMIN = "RBAC_ADMIN";
}
