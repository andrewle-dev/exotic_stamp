package metro.ExoticStamp.modules.auth.application.view;

import java.util.List;
import java.util.UUID;

public record AuthUserView(
        UUID id,
        String email,
        String username,
        List<String> roles
) {
}
