package metro.ExoticStamp.modules.community.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.modules.community.application.service.NotificationCommandService;
import metro.ExoticStamp.modules.community.application.service.NotificationQueryService;
import metro.ExoticStamp.modules.community.presentation.mapper.CommunityPresentationMapper;
import metro.ExoticStamp.modules.community.presentation.response.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;
    private final CommunityPresentationMapper presentationMapper;

    @GetMapping
    @Operation(summary = "List my notifications", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> list(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) Boolean unreadOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toNotificationPage(notificationQueryService.listMyNotifications(userId, unreadOnly, page, s))));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toNotificationResponse(notificationCommandService.markRead(userId, id))));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read (POST alias)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<NotificationResponse>> markReadPost(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id
    ) {
        return markRead(principal, id);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllRead(
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        int updated = notificationCommandService.markAllRead(userId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("updatedCount", updated)));
    }
}
