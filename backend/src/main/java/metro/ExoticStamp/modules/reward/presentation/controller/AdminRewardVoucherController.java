package metro.ExoticStamp.modules.reward.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.service.VoucherPoolCommandService;
import metro.ExoticStamp.modules.reward.application.service.VoucherPoolQueryService;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import metro.ExoticStamp.modules.reward.presentation.request.ImportVouchersRequest;
import metro.ExoticStamp.modules.reward.presentation.response.VoucherPoolResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/rewards/vouchers")
@RequiredArgsConstructor
@Tag(name = "Admin Voucher Pool")
public class AdminRewardVoucherController {

    private final VoucherPoolQueryService voucherPoolQueryService;
    private final VoucherPoolCommandService voucherPoolCommandService;
    private final RewardPresentationMapper presentationMapper;

    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('VOUCHER_POOL_MANAGE')")
    @Operation(summary = "Import voucher codes", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Map<String, Integer>>> importVouchers(
            @Valid @RequestBody ImportVouchersRequest request) {
        int count = voucherPoolCommandService.importVouchers(presentationMapper.toImportVouchersCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(Map.of("importedCount", count)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('VOUCHER_POOL_MANAGE')")
    @Operation(summary = "List vouchers", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<VoucherPoolResponse>>> list(
            @RequestParam(required = false) UUID milestoneId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toVoucherPoolPage(voucherPoolQueryService.list(milestoneId, status, page, s))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('VOUCHER_POOL_MANAGE')")
    @Operation(summary = "Get voucher", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VoucherPoolResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toVoucherPoolResponse(voucherPoolQueryService.get(id))));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('VOUCHER_POOL_MANAGE')")
    @Operation(summary = "Disable voucher", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<VoucherPoolResponse>> disable(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toVoucherPoolResponse(voucherPoolCommandService.disable(id))));
    }
}
