package metro.ExoticStamp.modules.community.application.port;

import java.util.UUID;

public interface UserVerificationPort {

    boolean isEmailVerified(UUID userId);
}
