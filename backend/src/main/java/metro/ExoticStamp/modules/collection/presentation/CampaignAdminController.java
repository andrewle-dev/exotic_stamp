package metro.ExoticStamp.modules.collection.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.service.CampaignCommandService;
import metro.ExoticStamp.modules.collection.application.service.CampaignQueryService;
import metro.ExoticStamp.modules.collection.application.service.CampaignStationCommandService;
import metro.ExoticStamp.modules.collection.application.service.CampaignStationQueryService;
import metro.ExoticStamp.modules.collection.presentation.dto.request.AssignCampaignStationRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.request.CreateCampaignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.request.UpdateCampaignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.response.CampaignResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.CampaignStationResponse;
import metro.ExoticStamp.modules.collection.presentation.mapper.CampaignPresentationMapper;
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
@RequestMapping("/api/v1/admin/campaigns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') and hasAuthority('CAMPAIGN_MANAGE')")
@Tag(name = "Admin Campaigns", description = "Campaign management and station assignment")
@SecurityRequirement(name = "bearerAuth")
public class CampaignAdminController {

    private final CampaignCommandService campaignCommandService;
    private final CampaignQueryService campaignQueryService;
    private final CampaignStationCommandService campaignStationCommandService;
    private final CampaignStationQueryService campaignStationQueryService;
    private final CampaignPresentationMapper mapper;

    @PostMapping
    @Operation(summary = "Create campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> create(@Valid @RequestBody CreateCampaignRequest request) {
        var view = campaignCommandService.create(mapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapper.toResponse(view)));
    }

    @GetMapping
    @Operation(summary = "List campaigns")
    public ResponseEntity<ApiResponse<PageResponse<CampaignResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toCampaignPage(campaignQueryService.list(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get campaign by id")
    public ResponseEntity<ApiResponse<CampaignResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(campaignQueryService.getById(id))));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update campaign")
    public ResponseEntity<ApiResponse<CampaignResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCampaignRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                mapper.toResponse(campaignCommandService.update(mapper.toUpdateCommand(id, request)))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete campaign")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        campaignCommandService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/{id}/stations")
    @Operation(summary = "Assign station to campaign")
    public ResponseEntity<ApiResponse<Void>> assignStation(
            @PathVariable UUID id,
            @Valid @RequestBody AssignCampaignStationRequest request
    ) {
        campaignStationCommandService.assign(id, request.getStationId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}/stations/{stationId}")
    @Operation(summary = "Remove station from campaign")
    public ResponseEntity<ApiResponse<Void>> removeStation(
            @PathVariable UUID id,
            @PathVariable UUID stationId
    ) {
        campaignStationCommandService.remove(id, stationId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/{id}/stations")
    @Operation(summary = "List stations assigned to campaign")
    public ResponseEntity<ApiResponse<List<CampaignStationResponse>>> listStations(@PathVariable UUID id) {
        List<CampaignStationResponse> stations = campaignStationQueryService.listByCampaignId(id).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(stations));
    }
}
