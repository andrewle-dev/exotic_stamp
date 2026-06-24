package metro.ExoticStamp.modules.collection.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.collection.application.service.ActiveCampaignQueryService;
import metro.ExoticStamp.modules.collection.presentation.dto.response.ActiveCampaignListResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.ActiveCampaignResponse;
import metro.ExoticStamp.modules.collection.presentation.dto.response.ActiveCampaignStationResponse;
import metro.ExoticStamp.modules.collection.presentation.mapper.CampaignPresentationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Public campaign read APIs")
public class CampaignPublicController {

    private final ActiveCampaignQueryService activeCampaignQueryService;
    private final CampaignPresentationMapper mapper;

    @GetMapping("/active")
    @Operation(summary = "List active campaigns with stations and stamp previews")
    public ResponseEntity<ApiResponse<ActiveCampaignListResponse>> listActive() {
        ActiveCampaignListResponse response = mapper.toActiveList(activeCampaignQueryService.listActive());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get active campaign by id")
    public ResponseEntity<ApiResponse<ActiveCampaignResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toActiveResponse(activeCampaignQueryService.getActiveById(id))));
    }

    @GetMapping("/{id}/stations")
    @Operation(summary = "List stations for an active campaign")
    public ResponseEntity<ApiResponse<List<ActiveCampaignStationResponse>>> listStations(@PathVariable UUID id) {
        List<ActiveCampaignStationResponse> stations = activeCampaignQueryService.listActiveStations(id).stream()
                .map(mapper::toActiveStationResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(stations));
    }
}
