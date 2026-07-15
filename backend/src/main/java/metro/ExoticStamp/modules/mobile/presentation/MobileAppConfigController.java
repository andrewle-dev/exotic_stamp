package metro.ExoticStamp.modules.mobile.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.mobile.application.service.MobileAppConfigQueryService;
import metro.ExoticStamp.modules.mobile.presentation.dto.response.MobileAppConfigResponse;
import metro.ExoticStamp.modules.mobile.presentation.mapper.MobileAppConfigPresentationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mobile")
@RequiredArgsConstructor
@Tag(name = "Mobile App Config", description = "Mobile client update and maintenance policy")
public class MobileAppConfigController {

    private final MobileAppConfigQueryService mobileAppConfigQueryService;
    private final MobileAppConfigPresentationMapper presentationMapper;

    @GetMapping("/app-config")
    @Operation(
            summary = "Get mobile app config and version policy",
            description = "Used by mobile clients to decide optional/forced update and maintenance state. "
                    + "Does not return the installed app version — that comes from the binary (e.g. package_info_plus)."
    )
    public ResponseEntity<ApiResponse<MobileAppConfigResponse>> getAppConfig() {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(mobileAppConfigQueryService.getAppConfig())));
    }
}
