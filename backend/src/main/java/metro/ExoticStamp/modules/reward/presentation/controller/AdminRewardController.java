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
import metro.ExoticStamp.modules.reward.presentation.request.BulkUploadVoucherRequest;
import metro.ExoticStamp.modules.reward.presentation.request.CreateRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdateRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.response.RewardResponse;
import metro.ExoticStamp.modules.reward.presentation.response.VoucherPoolStatsResponse;
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
@RequestMapping("/api/v1/admin/rewards")
@RequiredArgsConstructor
@Tag(name = "Admin Rewards")
public class AdminRewardController {

    private final AdminRewardQueryService adminRewardQueryService;
    private final AdminRewardCommandService adminRewardCommandService;
    private final RewardPresentationMapper presentationMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List rewards", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<RewardResponse>>> list(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toRewardPage(adminRewardQueryService.listRewards(activeOnly, page, s))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get reward", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RewardResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toRewardResponse(adminRewardQueryService.getReward(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create reward", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RewardResponse>> create(@Valid @RequestBody CreateRewardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toRewardResponse(
                        adminRewardCommandService.createReward(presentationMapper.toCreateRewardCommand(request)))));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update reward", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RewardResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRewardRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toRewardResponse(
                        adminRewardCommandService.updateReward(presentationMapper.toUpdateRewardCommand(id, request)))));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate reward", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RewardResponse>> activate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toRewardResponse(adminRewardCommandService.activateReward(id))));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate reward", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RewardResponse>> deactivate(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toRewardResponse(adminRewardCommandService.deactivateReward(id))));
    }

    @PostMapping("/{id}/vouchers/bulk-upload")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Bulk upload voucher codes", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VoucherPoolStatsResponse>> bulkUpload(
            @PathVariable UUID id,
            @Valid @RequestBody BulkUploadVoucherRequest request) {
        adminRewardCommandService.bulkUploadVouchers(id, request.getCodes());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toVoucherStatsResponse(adminRewardQueryService.getVoucherStats(id))));
    }

    @GetMapping("/{id}/vouchers/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Voucher pool stats", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VoucherPoolStatsResponse>> voucherStats(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toVoucherStatsResponse(adminRewardQueryService.getVoucherStats(id))));
    }
}
