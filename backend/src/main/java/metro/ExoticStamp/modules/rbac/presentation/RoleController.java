package metro.ExoticStamp.modules.rbac.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.rbac.application.PermissionCommandService;
import metro.ExoticStamp.modules.rbac.application.RoleCommandService;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.rbac.application.mapper.RoleAppMapper;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.AssignPermissionToRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.AssignRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.CreateRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.RevokeRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.UpdateRoleRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.PermissionResponse;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.RoleResponse;
import metro.ExoticStamp.modules.rbac.presentation.mapper.RolePresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "RBAC")
public class RoleController {

    private final RoleCommandService commandService;
    private final RoleQueryService queryService;
    private final PermissionCommandService permissionCommandService;
    private final RolePresentationMapper presentationMapper;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('RBAC_ADMIN')")
    @Operation(summary = "Create role")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest req) {
        var role = commandService.createRole(req.getRoleCode(), req.getDescription());
        return ResponseEntity.ok(ApiResponse.ok(presentationMapper.toRoleResponse(RoleAppMapper.toRoleView(role))));
    }

    @GetMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('RBAC_ADMIN')")
    @Operation(summary = "Get role by id")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable UUID roleId) {
        return ResponseEntity.ok(ApiResponse.ok(presentationMapper.toRoleResponse(queryService.getRoleById(roleId))));
    }

    @PatchMapping("/{roleId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('RBAC_ADMIN')")
    @Operation(summary = "Update role")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody UpdateRoleRequest req) {
        var role = commandService.updateRole(roleId, req.getRoleCode(), req.getDescription(), req.getStatus());
        return ResponseEntity.ok(ApiResponse.ok(presentationMapper.toRoleResponse(RoleAppMapper.toRoleView(role))));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all roles")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = queryService.getAllRoles().stream()
                .map(presentationMapper::toRoleResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }

    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List roles assigned to a user")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(@PathVariable UUID userId) {
        List<RoleResponse> roles = queryService.getRolesByUserId(userId).stream()
                .map(presentationMapper::toRoleResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(roles));
    }

    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign role to user")
    public ResponseEntity<ApiResponse<Void>> assignRole(@Valid @RequestBody AssignRoleRequest req) {
        commandService.assignRole(presentationMapper.toAssignCommand(req));
        return ResponseEntity.ok(ApiResponse.ok("Role assigned successfully", null));
    }

    @PostMapping("/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoke role from user")
    public ResponseEntity<ApiResponse<Void>> revokeRole(@Valid @RequestBody RevokeRoleRequest req) {
        commandService.revokeRole(presentationMapper.toRevokeCommand(req));
        return ResponseEntity.ok(ApiResponse.ok("Role revoked successfully", null));
    }

    @GetMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List permissions of a role")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions(@PathVariable UUID roleId) {
        List<PermissionResponse> permissions = queryService.getPermissionsByRoleId(roleId).stream()
                .map(presentationMapper::toPermissionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(permissions));
    }

    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('RBAC_ADMIN')")
    @Operation(summary = "Assign permission to role")
    public ResponseEntity<ApiResponse<Void>> assignPermissionToRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody AssignPermissionToRoleRequest req) {
        permissionCommandService.assignPermissionToRole(roleId, req.getPermissionCode());
        return ResponseEntity.ok(ApiResponse.ok("Permission assigned to role", null));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('RBAC_ADMIN')")
    @Operation(summary = "Revoke permission from role")
    public ResponseEntity<ApiResponse<Void>> revokePermissionFromRole(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId) {
        permissionCommandService.revokePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.ok("Permission revoked from role", null));
    }
}
