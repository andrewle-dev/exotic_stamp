package metro.ExoticStamp.modules.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.modules.user.application.UserCommandService;
import metro.ExoticStamp.modules.user.application.UserQueryService;
import metro.ExoticStamp.modules.user.presentation.dto.request.CreateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.request.UpdateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.response.UserResponse;
import metro.ExoticStamp.modules.user.presentation.mapper.UserPresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {

    private final UserCommandService commandService;
    private final UserQueryService queryService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(
                UserPresentationMapper.toResponse(queryService.getByUsername(principal.getUsername())));
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile (self-service fields only)")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateUserRequest req
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.ok(UserPresentationMapper.toResponse(commandService.updateUser(
                UserPresentationMapper.toUpdateCommand(userId, req))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('MANAGE_USER')")
    @Operation(summary = "Get user by id (admin)")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserPresentationMapper.toResponse(queryService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('MANAGE_USER')")
    @Operation(summary = "Create a new user (admin)")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        UserResponse response = UserPresentationMapper.toResponse(
                commandService.createUser(UserPresentationMapper.toCreateCommand(req)));
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('MANAGE_USER')")
    @Operation(summary = "Update a user (admin)")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest req
    ) {
        return ResponseEntity.ok(UserPresentationMapper.toResponse(commandService.updateUser(
                UserPresentationMapper.toUpdateCommand(id, req))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('MANAGE_USER')")
    @Operation(summary = "Soft-delete a user (admin)")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        commandService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
