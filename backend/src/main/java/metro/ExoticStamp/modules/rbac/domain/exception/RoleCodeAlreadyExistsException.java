package metro.ExoticStamp.modules.rbac.domain.exception;

public class RoleCodeAlreadyExistsException extends RuntimeException {

    public RoleCodeAlreadyExistsException(String code) {
        super("Role code already exists: " + code);
    }
}
