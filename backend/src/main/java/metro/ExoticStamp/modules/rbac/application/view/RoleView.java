package metro.ExoticStamp.modules.rbac.application.view;

import java.util.UUID;

public record RoleView(
        UUID id,
        String role,
        String description,
        String status,
        boolean systemRole
) {
}
