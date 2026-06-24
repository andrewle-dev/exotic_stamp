package metro.ExoticStamp.modules.collection.presentation.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;
import metro.ExoticStamp.modules.collection.application.service.CollectionCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.presentation.mapper.CollectionResponseMapper;
import metro.ExoticStamp.modules.collection.presentation.request.CollectStampRequest;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampBookResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampCollectResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @deprecated Use {@link metro.ExoticStamp.modules.collection.presentation.CollectionRuntimeController} at /api/v1/collection.
 */
@Deprecated
@Hidden
@RestController
@RequestMapping("/api/v1/collections")
@RequiredArgsConstructor
@Tag(name = "Collections (Legacy)")
public class CollectionController {

    private final CollectionCommandService commandService;
    private final CollectionQueryService queryService;
    private final UserStampAppMapper userStampAppMapper;
    private final CollectionResponseMapper responseMapper;

    @PostMapping("/scan")
    @Operation(summary = "Scan station (NFC or QR) and collect stamp", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Stamp collected or idempotent replay"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or inactive station", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Campaign or station not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Conflict (already collected or idempotency)", content = @Content)
    })
    public ResponseEntity<ApiResponse<StampCollectResponse>> scanAndCollect(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CollectStampRequest req
    ) {
        UUID userId = extractUserId(principal);

        CollectStampCommand cmd;
        boolean hasNfc = req.getNfcTagId() != null && !req.getNfcTagId().isBlank();
        if (hasNfc) {
            cmd = CollectStampCommand.legacyNfc(
                    userId,
                    req.getIdempotencyKey(),
                    req.getNfcTagId(),
                    req.getDeviceFingerprint(),
                    req.getLatitude(),
                    req.getLongitude()
            );
        } else {
            cmd = CollectStampCommand.legacyQr(
                    userId,
                    req.getIdempotencyKey(),
                    req.getQrToken(),
                    req.getDeviceFingerprint(),
                    req.getLatitude(),
                    req.getLongitude()
            );
        }
        // Legacy path: supply default accuracy when GPS coords present
        if (req.getLatitude() != null && req.getLongitude() != null) {
            cmd = new CollectStampCommand(
                    cmd.userId(),
                    cmd.idempotencyKey(),
                    cmd.scanType(),
                    cmd.payload(),
                    cmd.latitude(),
                    cmd.longitude(),
                    BigDecimal.valueOf(50),
                    null,
                    null,
                    cmd.deviceFingerprint()
            );
        }

        StampCollectResponse res = responseMapper.toResponse(commandService.collectStamp(cmd));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res));
    }

    @GetMapping("/me/stamps")
    @Operation(summary = "List my stamps for a line (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<UserStampResponse>>> myStamps(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam UUID lineId,
            @RequestParam(required = false) UUID campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = extractUserId(principal);
        PageResponse<UserStampResponse> res = responseMapper.toUserStampPage(
                queryService.getMyStamps(userId, lineId, campaignId, page, size));
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/me/progress/{lineId}")
    @Operation(summary = "Progress for a line (% collected)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProgressResponse>> myProgress(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID lineId,
            @RequestParam(required = false) UUID campaignId
    ) {
        UUID userId = extractUserId(principal);
        ProgressResponse res = responseMapper.toResponse(queryService.getMyProgress(userId, lineId, campaignId));
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/me/history")
    @Operation(summary = "Recent stamp collection history (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<UserStampResponse>>> myHistory(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = extractUserId(principal);
        PageResponse<UserStampResponse> res = responseMapper.toUserStampPage(
                queryService.getMyHistory(userId, page, size));
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/me/stamp-book/{lineId}")
    @Operation(summary = "Stamp book grid for a line", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StampBookResponse>> myStampBook(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID lineId,
            @RequestParam(required = false) UUID campaignId
    ) {
        UUID userId = extractUserId(principal);
        StampBookResponse res = responseMapper.toResponse(queryService.getStampBook(userId, lineId, campaignId));
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    private UUID extractUserId(UserDetails principal) {
        return SecurityPrincipalSupport.requireUserId(principal);
    }
}
