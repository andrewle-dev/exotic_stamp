package metro.ExoticStamp.modules.rbac.domain;

public final class RbacAuditConstants {

    private RbacAuditConstants() {}

    public static final String TABLE_ROLES = "roles";
    public static final String TABLE_PERMISSIONS = "permissions";
    public static final String TABLE_ROLE_PERMISSIONS = "role_permissions";
    public static final String TABLE_USER_ROLES = "user_roles";

    public static final String ACTION_ROLE_CREATE = "ROLE_CREATE";
    public static final String ACTION_ROLE_UPDATE = "ROLE_UPDATE";
    public static final String ACTION_PERMISSION_CREATE = "PERMISSION_CREATE";
    public static final String ACTION_ROLE_PERMISSION_ASSIGN = "ROLE_PERMISSION_ASSIGN";
    public static final String ACTION_ROLE_PERMISSION_REVOKE = "ROLE_PERMISSION_REVOKE";
    public static final String ACTION_USER_ROLE_ASSIGN = "USER_ROLE_ASSIGN";
    public static final String ACTION_USER_ROLE_REVOKE = "USER_ROLE_REVOKE";
}
