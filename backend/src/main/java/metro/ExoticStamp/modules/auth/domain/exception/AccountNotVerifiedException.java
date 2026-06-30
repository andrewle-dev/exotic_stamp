package metro.ExoticStamp.modules.auth.domain.exception;

public class AccountNotVerifiedException extends RuntimeException {
    public AccountNotVerifiedException() {
        super("Please verify your account with the code sent to your email.");
    }
}
