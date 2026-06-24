package metro.ExoticStamp.modules.metro.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.StationResponse;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Metro Stations")
public class PublicMetroStationController {

    private final StationQueryService stationQueryService;
    private final MetroPresentationMapper presentationMapper;

    @GetMapping("/api/v1/metro/stations")
    @Operation(summary = "List active stations")
    public ResponseEntity<ApiResponse<List<StationResponse>>> list(
            @RequestParam(required = false) UUID lineId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toStationResponses(stationQueryService.getPublicStations(lineId))));
    }

    @GetMapping("/api/v1/metro/stations/{id}")
    @Operation(summary = "Get active station detail")
    public ResponseEntity<ApiResponse<StationDetailResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(stationQueryService.getPublicStationDetail(id))));
    }

    @GetMapping("/api/v1/metro/lines/{lineId}/stations")
    @Operation(summary = "List active stations on an active line")
    public ResponseEntity<ApiResponse<List<StationResponse>>> listByLine(@PathVariable UUID lineId) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toStationResponses(stationQueryService.getPublicStationsByLine(lineId))));
    }
}
