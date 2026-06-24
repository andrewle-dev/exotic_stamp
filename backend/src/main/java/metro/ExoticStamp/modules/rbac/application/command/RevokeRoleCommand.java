package metro.ExoticStamp.modules.rbac.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeRoleCommand {
    private UUID userId;
    private String roleName;
}