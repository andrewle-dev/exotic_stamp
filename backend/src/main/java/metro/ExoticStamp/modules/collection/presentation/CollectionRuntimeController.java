package metro.ExoticStamp.modules.collection.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.service.CollectionCommandService;
import metro.ExoticStamp.modules.collection.application.service.CollectionQueryService;
import metro.ExoticStamp.modules.collection.presentation.mapper.CollectionRuntimePresentationMapper;
import metro.ExoticStamp.modules.collection.presentation.request.RuntimeCollectStampRequest;
import metro.ExoticStamp.modules.collection.presentation.response.CollectStatusResponse;
import metro.ExoticStamp.modules.collection.presentation.response.CollectStampResponse;
import metro.ExoticStamp.modules.collection.presentation.response.ProgressResponse;
import metro.ExoticStamp.modules.collection.presentation.response.StampBookResponse;
import metro.ExoticStamp.modules.collection.presentation.response.UserStampResponse;
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
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
@Tag(name = "Collection Runtime")
public class CollectionRuntimeController {

    private final CollectionCommandService commandService;
    private final CollectionQueryService queryService;
    private final CollectionRuntimePresentationMapper mapper;

    @PostMapping("/collect")
    @Operation(summary = "Collect stamp via NFC/QR scan", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Stamp collected or idempotent replay"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request or GPS required", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Default campaign or stamp design not found", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Already collected or idempotency conflict", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "GPS or campaign eligibility failure", content = @Content)
    })
    public ResponseEntity<ApiResponse<CollectStampResponse>> collect(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody RuntimeCollectStampRequest req
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        CollectStampCommand cmd = new CollectStampCommand(
                userId,
                req.getIdempotencyKey(),
                req.getScanType(),
                req.getPayload(),
                req.getLatitude(),
                req.getLongitude(),
                req.getAccuracyMeters(),
                req.getDevicePlatform(),
                req.getAppVersion(),
                null
        );
        CollectStampResponse res = mapper.toResponse(commandService.collect(cmd));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(res));
    }

    @GetMapping("/collect/status")
    @Operation(
            summary = "Resolve collect outcome by idempotency key (read-only)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status resolved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Missing idempotencyKey", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated", content = @Content)
    })
    public ResponseEntity<ApiResponse<CollectStatusResponse>> collectStatus(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam UUID idempotencyKey
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(
                mapper.toStatusResponse(queryService.getCollectStatus(userId, idempotencyKey))));
    }

    @GetMapping("/progress")
    @Operation(summary = "Collection progress for default campaign", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProgressResponse>> progress(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) UUID lineId
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        ProgressResponse res = mapper.toProgressResponse(queryService.getMyProgress(userId, lineId, null));
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/stamp-book")
    @Operation(summary = "Stamp book for default campaign", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StampBookResponse>> stampBook(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) UUID lineId
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        StampBookResponse res = mapper.toResponse(queryService.getStampBook(userId, lineId));
        return ResponseEntity.ok(ApiResponse.ok(res));
    }

    @GetMapping("/my-stamps")
    @Operation(summary = "Paginated stamps for default campaign", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<UserStampResponse>>> myStamps(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) UUID lineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        PageResponse<UserStampResponse> res = mapper.toUserStampPage(
                queryService.getMyStamps(userId, lineId, page, size));
        return ResponseEntity.ok(ApiResponse.ok(res));
    }
}
