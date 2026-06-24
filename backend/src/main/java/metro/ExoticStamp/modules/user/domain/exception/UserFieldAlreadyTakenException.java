package metro.ExoticStamp.modules.user.domain.exception;

public class UserFieldAlreadyTakenException extends RuntimeException {

    public UserFieldAlreadyTakenException(String field, String value) {
        super("User already taken with " + field + ": " + value);
    }

}