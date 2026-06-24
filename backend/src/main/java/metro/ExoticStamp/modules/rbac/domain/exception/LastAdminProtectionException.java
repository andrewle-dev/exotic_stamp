package metro.ExoticStamp.modules.rbac.domain.exception;

public class LastAdminProtectionException extends RuntimeException {

    public LastAdminProtectionException() {
        super("Cannot remove the last active administrator for this role");
    }
}
