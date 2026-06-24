package metro.ExoticStamp.modules.rbac.application.view;

import java.util.UUID;

public record PermissionView(
        UUID id,
        String permission,
        String description
) {
}
