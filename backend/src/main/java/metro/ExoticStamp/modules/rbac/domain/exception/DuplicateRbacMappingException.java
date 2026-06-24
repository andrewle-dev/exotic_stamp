package metro.ExoticStamp.modules.rbac.domain.exception;

public class DuplicateRbacMappingException extends RuntimeException {

    public DuplicateRbacMappingException(String message) {
        super(message);
    }
}
