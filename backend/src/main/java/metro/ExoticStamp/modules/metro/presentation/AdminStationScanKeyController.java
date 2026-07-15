package metro.ExoticStamp.modules.metro.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.StationScanKeyCommandService;
import metro.ExoticStamp.modules.metro.application.StationScanKeyQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateStationScanKeyRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.RevokeStationScanKeyRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.VerifyStationScanKeyInstallationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationScanKeyCreatedResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationScanKeyResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationScanKeyVerifyResponse;
import metro.ExoticStamp.modules.metro.presentation.mapper.StationScanKeyPresentationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/admin/metro")
@RequiredArgsConstructor
@Tag(name = "Admin Station Scan Keys", description = "Production NFC/QR scan key lifecycle. "
        + "Multiple ACTIVE NFC keys per station are allowed (e.g. multiple gates). "
        + "payloadToWrite is returned only once at creation; mobile should write it as an NDEF URI record.")
public class AdminStationScanKeyController {

    private final StationScanKeyCommandService commandService;
    private final StationScanKeyQueryService queryService;
    private final StationScanKeyPresentationMapper presentationMapper;

    @PostMapping("/stations/{stationId}/scan-keys")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(
            summary = "Generate a station scan key (DRAFT)",
            description = "Generates a cryptographically random key, stores only key_hash and key_prefix. "
                    + "Returns payloadToWrite exactly once — copy or write it to the physical tag immediately.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationScanKeyCreatedResponse>> create(
            @PathVariable UUID stationId,
            @Valid @RequestBody CreateStationScanKeyRequest request) {
        StationScanKeyCreatedResponse body = presentationMapper.toCreatedResponse(
                commandService.create(presentationMapper.toCreateCommand(stationId, request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(body));
    }

    @GetMapping("/stations/{stationId}/scan-keys")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(
            summary = "List station scan keys (metadata only)",
            description = "Never returns raw key or payloadToWrite.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<StationScanKeyResponse>>> list(@PathVariable UUID stationId) {
        List<StationScanKeyResponse> body = queryService.listByStationId(stationId).stream()
                .map(presentationMapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(body));
    }

    @PatchMapping("/scan-keys/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(
            summary = "Activate a scan key",
            description = "Only DRAFT or INACTIVE keys can become ACTIVE. "
                    + "Multiple ACTIVE NFC keys per station are allowed.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationScanKeyResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(presentationMapper.toResponse(
                commandService.activate(presentationMapper.toActivateCommand(id)))));
    }

    @PatchMapping("/scan-keys/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Revoke a scan key", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationScanKeyResponse>> revoke(
            @PathVariable UUID id,
            @RequestBody(required = false) @Valid RevokeStationScanKeyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(presentationMapper.toResponse(
                commandService.revoke(presentationMapper.toRevokeCommand(id, request)))));
    }

    @PatchMapping("/scan-keys/{id}/mark-lost")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Mark a scan key as lost", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationScanKeyResponse>> markLost(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(presentationMapper.toResponse(commandService.markLost(id))));
    }

    @PostMapping("/scan-keys/{id}/verify-installation")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(
            summary = "Verify physical tag installation",
            description = "Parses payloadReadBack, verifies hash match, stores installation GPS/device metadata. "
                    + "Does not mutate station latitude/longitude.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationScanKeyVerifyResponse>> verifyInstallation(
            @PathVariable UUID id,
            @Valid @RequestBody VerifyStationScanKeyInstallationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(presentationMapper.toVerifyResponse(
                commandService.verifyInstallation(presentationMapper.toVerifyCommand(id, request)))));
    }
}
