package metro.ExoticStamp.modules.community.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.modules.community.application.service.ShareEventCommandService;
import metro.ExoticStamp.modules.community.application.service.ShareEventQueryService;
import metro.ExoticStamp.modules.community.presentation.mapper.CommunityPresentationMapper;
import metro.ExoticStamp.modules.community.presentation.request.RecordShareEventRequest;
import metro.ExoticStamp.modules.community.presentation.response.ShareEventResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/community/share-events")
@RequiredArgsConstructor
@Tag(name = "Community Share Events")
public class CommunityShareController {

    private final ShareEventCommandService shareEventCommandService;
    private final ShareEventQueryService shareEventQueryService;
    private final CommunityPresentationMapper presentationMapper;

    @PostMapping
    @Operation(summary = "Record a share event", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ShareEventResponse>> recordShare(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RecordShareEventRequest request
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toShareEventResponse(
                        shareEventCommandService.recordShare(userId, presentationMapper.toRecordShareEventCommand(request)))));
    }

    @GetMapping("/me")
    @Operation(summary = "List my share events", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<ShareEventResponse>>> listMyShares(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toShareEventPage(shareEventQueryService.listMyShares(userId, page, s))));
    }
}
