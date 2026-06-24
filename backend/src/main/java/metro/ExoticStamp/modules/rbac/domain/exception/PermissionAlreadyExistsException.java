package metro.ExoticStamp.modules.rbac.domain.exception;

public class PermissionAlreadyExistsException extends RuntimeException {

    public PermissionAlreadyExistsException(String code) {
        super("Permission already exists: " + code);
    }
}
