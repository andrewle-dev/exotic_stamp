package metro.ExoticStamp.modules.rbac.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.rbac.application.PermissionCommandService;
import metro.ExoticStamp.modules.rbac.application.PermissionQueryService;
import metro.ExoticStamp.modules.rbac.application.mapper.RoleAppMapper;
import metro.ExoticStamp.modules.rbac.presentation.dto.request.CreatePermissionRequest;
import metro.ExoticStamp.modules.rbac.presentation.dto.response.PermissionResponse;
import metro.ExoticStamp.modules.rbac.presentation.mapper.RolePresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "RBAC")
public class PermissionController {

    private final PermissionQueryService permissionQueryService;
    private final PermissionCommandService permissionCommandService;
    private final RolePresentationMapper presentationMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('RBAC_ADMIN')")
    @Operation(summary = "List permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> listPermissions() {
        List<PermissionResponse> permissions = permissionQueryService.listPermissions().stream()
                .map(presentationMapper::toPermissionResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(permissions));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('RBAC_ADMIN')")
    @Operation(summary = "Create permission")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest req) {
        var saved = permissionCommandService.createPermission(req.getPermissionCode(), req.getDescription());
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toPermissionResponse(RoleAppMapper.toPermissionView(saved))));
    }
}
