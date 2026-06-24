package metro.ExoticStamp.modules.collection.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.collection.application.service.ActiveCampaignQueryService;
import metro.ExoticStamp.modules.collection.presentation.dto.response.ActiveCampaignResponse;
import metro.ExoticStamp.modules.collection.presentation.mapper.CampaignPresentationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
@Tag(name = "Station Campaigns", description = "Campaign eligibility by station")
public class StationCampaignController {

    private final ActiveCampaignQueryService activeCampaignQueryService;
    private final CampaignPresentationMapper mapper;

    @GetMapping("/{stationId}/campaigns")
    @Operation(summary = "List active campaigns that include this station")
    public ResponseEntity<ApiResponse<List<ActiveCampaignResponse>>> listCampaignsForStation(
            @PathVariable UUID stationId
    ) {
        List<ActiveCampaignResponse> campaigns = activeCampaignQueryService.listActiveByStationId(stationId).stream()
                .map(mapper::toActiveResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(campaigns));
    }
}
