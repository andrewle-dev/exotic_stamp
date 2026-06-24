package metro.ExoticStamp.modules.auth.domain.exception;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String msg) {
        super(msg);
    }
}

