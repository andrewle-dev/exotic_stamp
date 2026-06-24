package metro.ExoticStamp.modules.rbac.presentation.mapper;

import metro.ExoticStamp.modules.rbac.application.command.AssignRoleCommand;
import metro.ExoticStamp.modules.rbac.application.command.RevokeRoleCommand;
import metro.ExoticStamp.modules.rbac.application.view.PermissionView;
import metro.ExoticStamp.modules.rbac.application.view.RoleView;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.AssignRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.RevokeRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.PermissionResponse;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.RoleResponse;
import org.springframework.stereotype.Component;

@Component
public class RolePresentationMapper {

    public AssignRoleCommand toAssignCommand(AssignRoleRequest request) {
        return AssignRoleCommand.builder()
                .userId(request.getUserId())
                .roleName(request.getRoleName())
                .build();
    }

    public RevokeRoleCommand toRevokeCommand(RevokeRoleRequest request) {
        return RevokeRoleCommand.builder()
                .userId(request.getUserId())
                .roleName(request.getRoleName())
                .build();
    }

    public RoleResponse toRoleResponse(RoleView view) {
        return RoleResponse.builder()
                .id(view.id())
                .role(view.role())
                .description(view.description())
                .status(view.status())
                .systemRole(view.systemRole())
                .build();
    }

    public PermissionResponse toPermissionResponse(PermissionView view) {
        return PermissionResponse.builder()
                .id(view.id())
                .permission(view.permission())
                .description(view.description())
                .build();
    }
}
