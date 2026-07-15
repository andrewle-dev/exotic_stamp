package metro.ExoticStamp.modules.metro.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.metro.application.StationCommandService;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.application.command.RotateStationQrCommand;
import metro.ExoticStamp.modules.metro.presentation.dto.MetroStatusApi;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateStationRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ReorderStationsRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateScanKeysRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateStationRequest;
import metro.ExoticStamp.common.reorder.ReorderResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationStatsResponse;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/metro/stations")
@RequiredArgsConstructor
@Tag(name = "Admin Metro Stations")
public class AdminMetroStationController {

    private final StationQueryService stationQueryService;
    private final StationCommandService stationCommandService;
    private final MetroPresentationMapper presentationMapper;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Station scan statistics", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<StationStatsResponse>>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toStationStatsResponses(stationQueryService.stationStats())));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "List stations (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<StationResponse>>> list(
            @RequestParam(required = false) UUID lineId,
            @RequestParam(required = false) MetroStatusApi status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        int s = size != null ? size : 20;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toStationPage(
                        stationQueryService.searchAdminStations(lineId, status != null ? status.name() : null, search, page, s, sort))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Get station detail (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationDetailResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(stationQueryService.getAdminStationDetail(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Create station", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationDetailResponse>> create(@Valid @RequestBody CreateStationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toResponse(stationCommandService.createStation(
                        presentationMapper.toCreateStationCommand(request)))));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Update station", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationDetailResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(stationCommandService.updateStation(
                        presentationMapper.toUpdateStationCommand(id, request)))));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Reorder stations on a line",
            description = "Dense-renumbers all stations on the given line to 0..n-1. "
                    + "orderedIds must be a permutation of every station id on that line (all statuses).",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReorderResponse>> reorder(@Valid @RequestBody ReorderStationsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(stationCommandService.reorderStations(
                        presentationMapper.toReorderStationsCommand(request)))));
    }

    @PatchMapping("/{id}/scan-keys")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Update station scan keys", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationDetailResponse>> updateScanKeys(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateScanKeysRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(stationCommandService.updateScanKeys(
                        presentationMapper.toUpdateScanKeysCommand(id, request)))));
    }

    @PostMapping("/{id}/rotate-qr")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Rotate station QR value", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<StationDetailResponse>> rotateQr(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(stationCommandService.rotateQr(
                        RotateStationQrCommand.builder().stationId(id).build()))));
    }

    @PatchMapping("/{id}/collector-count")
    @PreAuthorize("hasAuthority('INTERNAL')")
    @Operation(summary = "Increment collector count (internal)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> incrementCollectorCount(@PathVariable UUID id) {
        stationCommandService.incrementCollectorCount(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_STATION_MANAGE')")
    @Operation(summary = "Soft-delete station", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        stationCommandService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}
