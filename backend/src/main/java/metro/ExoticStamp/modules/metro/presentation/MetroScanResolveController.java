package metro.ExoticStamp.modules.metro.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.MetroScanResolveService;
import metro.ExoticStamp.modules.metro.presentation.dto.request.ScanResolveRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.ScanResolveResponse;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/metro/scan")
@RequiredArgsConstructor
@Tag(name = "Metro Scan")
public class MetroScanResolveController {

    private final MetroScanResolveService scanResolveService;
    private final MetroPresentationMapper presentationMapper;

    @PostMapping("/resolve")
    @Operation(summary = "Resolve NFC/QR payload to station metadata")
    public ResponseEntity<ApiResponse<ScanResolveResponse>> resolve(@Valid @RequestBody ScanResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(scanResolveService.resolve(
                        presentationMapper.toScanResolveCommand(request)))));
    }
}
