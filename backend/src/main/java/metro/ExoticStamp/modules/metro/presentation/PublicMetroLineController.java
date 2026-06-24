package metro.ExoticStamp.modules.metro.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/metro/lines")
@RequiredArgsConstructor
@Tag(name = "Metro Lines")
public class PublicMetroLineController {

    private final LineQueryService lineQueryService;
    private final MetroPresentationMapper presentationMapper;

    @GetMapping
    @Operation(summary = "List active metro lines")
    public ResponseEntity<ApiResponse<List<LineResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toLineResponses(lineQueryService.getPublicLines())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get active metro line with stations")
    public ResponseEntity<ApiResponse<LineDetailResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(lineQueryService.getPublicLineDetail(id))));
    }
}
