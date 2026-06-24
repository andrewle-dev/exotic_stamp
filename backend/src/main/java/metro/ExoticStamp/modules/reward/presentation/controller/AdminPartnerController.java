package metro.ExoticStamp.modules.reward.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardQueryService;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import metro.ExoticStamp.modules.reward.presentation.request.CreatePartnerRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdatePartnerRequest;
import metro.ExoticStamp.modules.reward.presentation.response.PartnerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/partners")
@RequiredArgsConstructor
@Tag(name = "Admin Partners")
public class AdminPartnerController {

    private final AdminRewardQueryService adminRewardQueryService;
    private final AdminRewardCommandService adminRewardCommandService;
    private final RewardPresentationMapper presentationMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List partners", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<PartnerResponse>>> list(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toPartnerPage(adminRewardQueryService.listPartners(activeOnly, page, s))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get partner", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PartnerResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toPartnerResponse(adminRewardQueryService.getPartner(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create partner", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PartnerResponse>> create(@Valid @RequestBody CreatePartnerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toPartnerResponse(
                        adminRewardCommandService.createPartner(presentationMapper.toCreatePartnerCommand(request)))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update partner", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PartnerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePartnerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toPartnerResponse(
                        adminRewardCommandService.updatePartner(presentationMapper.toUpdatePartnerCommand(id, request)))));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate partner", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PartnerResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toPartnerResponse(adminRewardCommandService.activatePartner(id))));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate partner", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PartnerResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toPartnerResponse(adminRewardCommandService.deactivatePartner(id))));
    }
}
